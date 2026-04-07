# UMO — University Medical Offices

> Plataforma backend para la gestión de reservas de consultorios médicos universitarios.  
> Construida con Java 21 · Spring Boot 4 · PostgreSQL · Testcontainers · JUnit 5 · Mockito.

---

## Descripción general

UMO es una API REST que permite administrar el ciclo completo de atención médica universitaria: registro de pacientes y profesionales, configuración de horarios, consulta de disponibilidad, gestión de citas y generación de reportes operativos.

El proyecto va más allá del alcance mínimo del taller. Se aplicaron decisiones de modelado y diseño que reflejan un sistema más cercano a un escenario real: jerarquía de personas, modelo de usuarios y roles, enums tipados para todos los estados del dominio, trazabilidad completa de citas y configuración robusta de la infraestructura de pruebas.

---

## Stack tecnológico

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 4.x | Framework de aplicación |
| Spring Data JPA | — | Abstracción de persistencia |
| Hibernate ORM | 7.x | Implementación JPA |
| PostgreSQL | 16 | Base de datos relacional |
| Testcontainers | latest | Contenedor Docker PostgreSQL para tests de integración |
| JUnit 5 | latest | Framework de pruebas |
| Mockito | latest | Mocking en pruebas unitarias |
| AssertJ | latest | Aserciones fluidas en tests |
| Lombok | latest | Reducción de boilerplate con `@Builder`, `@SuperBuilder`, `@RequiredArgsConstructor` |
| MapStruct | latest | Mapeo automático entidad ↔ DTO |
| Maven | 3.x | Gestión de dependencias y ciclo de build |

---

## Requisitos previos

- **Java 21** instalado y configurado en `JAVA_HOME`
- **Docker Desktop** corriendo antes de ejecutar los tests (requerido por Testcontainers)
- **Maven 3.x** disponible en el `PATH`

> No es necesario instalar PostgreSQL localmente. Los tests de integración levantan automáticamente un contenedor PostgreSQL 16 temporal y lo destruyen al finalizar.

---

## Cómo ejecutar el proyecto

### Compilar el proyecto

```bash
./mvnw clean compile
```

### Ejecutar todos los tests

```bash
./mvnw clean test
```

### Ejecutar solo tests de repositorio (integración con Testcontainers)

```bash
./mvnw test -Dtest="*RepositoryIT,*RepositoryIntegrationTest"
```

### Ejecutar solo tests de servicio (unitarios con Mockito)

```bash
./mvnw test -Dtest="*ServiceImplTest"
```

### Construir el artefacto final

```bash
./mvnw clean package -DskipTests
```

---

## Estructura del proyecto

```
src/
├── main/java/unimag/proyect/
│   ├── entities/           # Entidades JPA: Person, Patient, Doctor, SystemUser, Role,
│   │                       # Specialty, Office, AppointmentType, DoctorSchedule, Appointment
│   ├── enums/              # Enumeraciones de dominio tipadas
│   │                       # DocumentType, PersonStatus, Gender, OfficeStatus,
│   │                       # ScheduleStatus, WeekDay, AppointmentStatus
│   ├── repositories/       # Interfaces Spring Data JPA con Query Methods y JPQL
│   ├── services/           # Interfaces de servicio (contrato del dominio)
│   │   └── impl/           # Implementaciones con lógica de negocio y validaciones
│   ├── dto/
│   │   ├── request/        # DTOs de entrada validados
│   │   └── response/       # DTOs de salida proyectados
│   ├── mappers/            # Mappers MapStruct: entidad ↔ DTO
│   ├── exceptions/         # Excepciones de dominio: ResourceNotFoundException,
│   │                       # BusinessException, ConflictException
│   └── config/             # Configuración de la aplicación y seguridad
└── test/java/unimag/proyect/
    ├── repositories/       # Tests de integración con Testcontainers + PostgreSQL real
    └── services/           # Tests unitarios con Mockito (modo estricto)
```

---

## Modelo de datos

### Extensiones sobre el mínimo del taller

El taller exige las entidades `Patient`, `Doctor`, `Specialty`, `Office`, `AppointmentType`, `DoctorSchedule` y `Appointment`. Este proyecto amplía ese modelo con las siguientes decisiones de diseño:

#### 1. Superentidad `Person` con generalización overlapping

En lugar de modelar `Patient` y `Doctor` como entidades aisladas y repetir datos de identidad en cada una, se definió `Person` como entidad abstracta base con los atributos comunes:

```
Person (abstracta)
├── full_name
├── document_type  → enum DocumentType (CC, TI, CE, PASSPORT, NIT)
├── document_number (unique)
├── email (unique)
├── phone
├── gender         → enum Gender (MALE, FEMALE)
└── status         → enum PersonStatus (ACTIVE, INACTIVE)
```

`Patient`, `Doctor` y `SystemUser` extienden `Person` usando `InheritanceType.JOINED`, lo que genera una tabla `person` central y tablas hijas con solo los atributos propios de cada subentidad. Esto permite que una misma persona pueda tener múltiples roles en el sistema (por ejemplo, ser doctor y paciente al mismo tiempo), sin duplicar sus datos de identidad.

#### 2. `SystemUser` y `Role` — modelo de usuarios del sistema

Aunque el enunciado menciona distintos actores (administrador, recepcionista, profesional de salud, coordinador), no obliga a implementar usuarios del sistema. Este proyecto los agrega como extensión:

- **`SystemUser`** extiende `Person` y añade los atributos de acceso: `username` (unique), `password` y `profile_photo`.
- **`Role`** es una entidad independiente que representa el rol del actor en el sistema: `ADMIN`, `RECEPTIONIST`, `DOCTOR`, `COORDINATOR`.
- La relación `SystemUser → Role` es muchos-a-uno: un usuario tiene un rol asignado.

> **Nota técnica:** La tabla se nombró `sys_user` en lugar de `system_user` porque `system_user` es una palabra reservada en PostgreSQL y causaba errores de sintaxis al generar el DDL.

Esta separación entre datos humanos (`Person`) y datos de acceso (`SystemUser`) prepara la base para una integración futura con Spring Security y JWT.

#### 3. Separación entre `code` y `name` en `Office`

El taller pide registrar consultorios evitando duplicados. Se decidió separar dos atributos con semántica diferente:

- `Office.code` — identificador de negocio único (e.g., `CONS-101`). Es el campo inmutable que identifica el espacio físico.
- `Office.name` — nombre descriptivo legible (e.g., `Consultorio de Psicología`). Puede cambiar sin afectar la identidad del consultorio.

#### 4. `status` en `DoctorSchedule`

El taller solo exige gestionar el día de la semana y el rango horario de cada doctor. Se añadió `DoctorSchedule.status` con el enum `ScheduleStatus (AVAILABLE, UNAVAILABLE)` para permitir desactivar temporalmente una franja horaria sin borrar el registro, manteniendo trazabilidad de la configuración histórica.

#### 5. `endTime` derivado y trazabilidad completa en `Appointment`

- `Appointment.endTime` es calculado exclusivamente por el servidor: `endTime = startTime + AppointmentType.duration`. El cliente nunca puede enviarlo.
- Se incluyen los atributos de trazabilidad `cancel_reason` y `observations` para soportar el ciclo completo de estados.

### Tablas del sistema

| Tabla | Descripción |
|---|---|
| `person` | Datos de identidad compartidos por pacientes, doctores y usuarios del sistema |
| `patients` | Extensión de `person` con datos específicos del paciente |
| `doctors` | Extensión de `person` con número de registro y especialidad |
| `sys_user` | Extensión de `person` con credenciales de acceso al sistema |
| `role` | Catálogo de roles del sistema (ADMIN, RECEPTIONIST, DOCTOR, COORDINATOR) |
| `specialties` | Catálogo de especialidades médicas |
| `offices` | Consultorios físicos con código, nombre, ubicación y estado |
| `appointment_types` | Tipos de cita con duración definida en minutos |
| `doctor_schedules` | Franjas horarias de atención por doctor, día de la semana y estado |
| `appointments` | Reservas médicas con estado, trazabilidad y rango temporal calculado |

### Enums de dominio

| Enum | Valores | Entidad |
|---|---|---|
| `DocumentType` | `CC, TI, CE, PASSPORT, NIT` | `Person` |
| `PersonStatus` | `ACTIVE, INACTIVE` | `Person` |
| `Gender` | `MALE, FEMALE` | `Person` |
| `OfficeStatus` | `AVAILABLE, UNAVAILABLE, MAINTENANCE` | `Office` |
| `ScheduleStatus` | `AVAILABLE, UNAVAILABLE` | `DoctorSchedule` |
| `WeekDay` | `MONDAY … SUNDAY` | `DoctorSchedule` |
| `AppointmentStatus` | `SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW` | `Appointment` |

> Todos los enums se persisten con `@Enumerated(EnumType.STRING)`. Esto garantiza que la base de datos almacene el texto del valor, no su índice numérico, haciendo los datos legibles y resistentes a reordenamientos futuros del enum.

---

## Reglas de negocio

### Creación de citas

1. La fecha y hora no puede ser en el pasado.
2. El paciente debe existir y estar `ACTIVE`.
3. El doctor debe existir y estar `ACTIVE`.
4. El consultorio debe existir y estar `ACTIVE`.
5. La cita debe caer dentro del horario laboral configurado para el doctor en el día correspondiente.
6. `endTime` es calculado por el servidor: `endTime = startTime + duración del tipo de cita`.
7. No puede haber traslape de horario para el doctor en el mismo rango temporal.
8. No puede haber traslape de horario para el consultorio en el mismo rango temporal.
9. Un paciente no puede tener dos citas activas que se crucen en el tiempo.
10. Toda cita nueva se crea con estado inicial `SCHEDULED`.

### Máquina de estados de una cita

```
SCHEDULED ──► CONFIRMED ──► COMPLETED
    │               │
    └───────────────┴──► CANCELLED
                    └──► NO_SHOW
```

| Transición | Estado origen válido | Condición extra |
|---|---|---|
| `confirm` | `SCHEDULED` | — |
| `cancel` | `SCHEDULED` o `CONFIRMED` | Motivo de cancelación obligatorio |
| `complete` | `CONFIRMED` | Hora actual ≥ `startTime` |
| `markNoShow` | `CONFIRMED` | Hora actual ≥ `startTime` |

### Disponibilidad y reportes

- Los slots disponibles se calculan fragmentando el horario laboral del doctor en bloques de duración exacta del tipo de cita solicitado.
- Solo se devuelven bloques completos y sin conflicto; no aproximaciones.
- La ocupación de consultorios se calcula sobre bloques de 30 minutos entre las 08:00 y las 18:00 (20 slots teóricos por día).
- Los reportes de productividad médica ordenan por citas `COMPLETED` en un rango de fechas.
- Los reportes de inasistencia identifican pacientes con mayor cantidad de marcaciones `NO_SHOW`.

---

## Decisiones de diseño

### Arquitectura por capas estricta

El proyecto aplica separación de responsabilidades en capas sin mezclar lógica entre ellas:
- **Repositorios**: solo consultan la base de datos.
- **Servicios**: controlan el flujo, aplican reglas de negocio y orquestan las llamadas al repositorio.
- **Mappers**: únicamente transforman entre entidades y DTOs, sin lógica de negocio.

### Lombok + MapStruct — orden crítico en Maven

Se usa Lombok para reducir boilerplate y MapStruct para el mapeo automático. El orden de `annotationProcessorPaths` en el `maven-compiler-plugin` es **obligatorio**: Lombok debe declararse antes que MapStruct. Si se invierte, MapStruct no puede ver los getters y setters generados por Lombok en tiempo de compilación y falla con errores `No property named X exists`.

```xml
<annotationProcessorPaths>
    <!-- Lombok PRIMERO -->
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <!-- MapStruct SEGUNDO -->
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

Las entidades con herencia usan `@SuperBuilder` de Lombok en lugar de `@Builder` estándar, lo que permite que los builders de las subclases incluyan los campos de la clase padre.

### Excepciones de dominio tipadas

Se definieron tres excepciones custom que expresan el tipo exacto de fallo:

| Excepción | Semántica | Código HTTP de referencia |
|---|---|---|
| `ResourceNotFoundException` | Entidad no encontrada por ID | 404 |
| `BusinessException` | Regla de negocio violada | 400 |
| `ConflictException` | Traslape de horario o conflicto de datos | 409 |

Cada excepción tiene dos constructores: uno con mensaje directo y otro con entidad e identificador, para mensajes de error consistentes y descriptivos.

### Transaccionalidad por nivel de lectura/escritura

Todos los `ServiceImpl` son `@Transactional` a nivel de clase. Los métodos de solo lectura llevan adicionalmente `@Transactional(readOnly = true)` para permitir optimizaciones en Hibernate (sin dirty checking) y en el pool de conexiones.

### Testcontainers en lugar de H2

Los tests de integración de repositorio usan PostgreSQL 16 real en Docker en lugar de H2 en memoria. La razón es que H2, incluso en modo de compatibilidad, no replica con exactitud el comportamiento de PostgreSQL: ignora ciertos constraints, acepta tipos que PostgreSQL rechaza y no valida enums de la misma forma. Con Testcontainers los tests fallan exactamente por las mismas razones que fallarían en producción.

La clase base `AbstractRepositoryIT` levanta el contenedor una sola vez por suite completa (contenedor estático), reduciendo el tiempo total de ejecución.

### Tests unitarios con Mockito en modo estricto

Los tests de servicio usan `@ExtendWith(MockitoExtension.class)` con detección estricta de stubs innecesarios (`UnnecessaryStubbingException`). Esto obliga a que cada mock declarado en un test sea efectivamente utilizado, mejorando la calidad y la precisión de cada caso de prueba.

Los mocks de `DoctorSchedule` usan `toWeekDay(start.getDayOfWeek())` en lugar de un `WeekDay` fijo, para que el horario simulado coincida siempre con el día real en que se ejecuta el test y evitar fallos no deterministas por día de la semana.

---

## Pruebas automatizadas

### Tests de integración — Repositorio: **93 / 93 ✅**

Cubren los 9 repositorios del sistema con PostgreSQL real:

| Repositorio | Tests | Consultas verificadas |
|---|---|---|
| `DoctorRepository` | ✅ | findByIdWithSchedules, findBySpeciality, findByPartialName, findByStatus |
| `PatientRepository` | ✅ | findByIdWithAppointments, findByDoctorId, findByAppointmentStatus |
| `AppointmentRepository` | ✅ | findByDoctorId, findByPatientId, findByStatus, detectDoctorConflict, detectOfficeConflict |
| `DoctorScheduleRepository` | ✅ | findByDoctorId, findByWeekDay, findActiveByDoctor, detectConflict |
| `OfficeRepository` | ✅ | findByCode, findByName, findByStatus, findAvailable |
| `SpecialityRepository` | ✅ | findWithActiveDoctors |
| `AppointmentTypeRepository` | ✅ | findTypesWithScheduledAppointments |
| `SystemUserRepository` | ✅ | findByUsername, findByEmail, findByRole, findByStatus |

### Tests unitarios — Servicios (Mockito)

**`AppointmentServiceImplTest`**
- Rechazo de cita en el pasado → `BusinessException`
- Rechazo por horario fuera de jornada del doctor → `BusinessException`
- Rechazo por conflicto de doctor → `ConflictException`
- Rechazo por conflicto de consultorio → `ConflictException`
- Cálculo correcto de `endTime` y estado inicial `SCHEDULED`
- Cancelación válida desde `SCHEDULED` y `CONFIRMED`
- Rechazo de cancelación sobre cita `COMPLETED`
- Cierre correcto → `COMPLETED`
- Marcación de no asistencia → `NO_SHOW`
- Rechazo de `NO_SHOW` cuando el estado no es `CONFIRMED`

**`AvailabilityServiceImplTest`**
- Tipo de cita inexistente → `ResourceNotFoundException`
- Generación correcta de slots libres dentro del horario del doctor

**`DoctorScheduleServiceImplTest`**
- Rechazo cuando `startTime >= endTime` → `BusinessException`
- Rechazo cuando el doctor no existe → `ResourceNotFoundException`
- Rechazo por conflicto de horario en el mismo día → `ConflictException`
- Creación exitosa con estado inicial `AVAILABLE`
- Delegación correcta al repositorio y al mapper

---

## Alcance de la entrega

> Según la indicación del profesor, esta entrega cubre las capas de **Repository** y **Service** con sus respectivos tests automatizados. La capa Controller, el GlobalExceptionHandler y los tests MockMvc quedan fuera del alcance evaluado en esta entrega.

---

## Autor

Proyecto desarrollado para el curso de Programación Web — Universidad del Magdalena, 2026.
