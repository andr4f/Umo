# Umo

University Medical Offices

\## Extensiones sobre el alcance mínimo del taller



El taller define un alcance funcional mínimo: registrar pacientes, doctores, especialidades, consultorios, tipos de cita, horarios de atención y citas médicas; además de consultar disponibilidad y generar algunos reportes básicos \[file:52]. Sobre ese mínimo, este proyecto introduce varias mejoras de modelado:



1\. \*\*Superentidad `Person` con especializaciones overlapping\*\*



&#x20;  - En lugar de modelar `Patient` y `Doctor` como entidades aisladas, se definió la superentidad `Person` con los datos de identidad y contacto compartidos (`full\_name`, `document\_type`, `document\_number`, `email`, `phone`, `status`).

&#x20;  - A partir de `Person` se especializan `Patient`, `Doctor` y `SystemUser` usando generalización overlapping, permitiendo que una misma persona pueda ser, por ejemplo, doctor y paciente al mismo tiempo.

&#x20;  - Esto reduce duplicidad de datos y refleja mejor la realidad del dominio, sin que el taller lo exigiera explícitamente.



2\. \*\*Modelo explícito de usuarios del sistema y roles\*\*



&#x20;  - Aunque el enunciado menciona distintas perspectivas (administrador, recepcionista, profesional de salud, coordinador), no obliga a implementar autenticación/autorización ni clases de usuario \[file:52].

&#x20;  - Este diseño añade:

&#x20;    - `SystemUser` como subentidad de `Person`, con atributos técnicos (`username`, `password`, `profile\_photo`).

&#x20;    - `Role` como entidad independiente, con la relación `SystemUser ─ assigned\_to ─ Role`. Los valores de `Role` representan los actores de las historias de usuario (`ADMIN`, `RECEPTIONIST`, `DOCTOR`, `COORDINATOR`).

&#x20;  - De esta forma se separan claramente los datos humanos (en `Person`) de los datos de acceso al sistema (en `SystemUser`), preparando el terreno para una futura integración con Spring Security.



3\. \*\*Separación entre código y nombre de consultorio\*\*



&#x20;  - El enunciado pide registrar consultorios con nombre o código, evitando duplicados \[file:52].

&#x20;  - En el modelo se decidió separar `Office.code` (identificador de negocio único del consultorio) y `Office.name` (descripción legible), lo que permite tener un código estable aunque cambie el nombre visible.



4\. \*\*Estado en los horarios de atención (`DoctorSchedule`)\*\*



&#x20;  - El taller solo exige gestionar día de la semana y rango horario para cada doctor \[file:52].

&#x20;  - Se añadió el atributo `status` en `DoctorSchedule` para permitir desactivar temporalmente una franja (por ejemplo, si el doctor se ausenta un día) sin borrar el registro, manteniendo trazabilidad de la configuración histórica.



5\. \*\*Atributos derivados y de trazabilidad en las citas\*\*



&#x20;  - El enunciado indica que la hora de fin de la cita debe calcularse a partir de la duración del tipo de cita y que toda nueva cita se crea en estado `SCHEDULED` \[file:52].

&#x20;  - En el modelo conceptual esto se refleja con:

&#x20;    - `Appointment.endTime` como atributo \*\*derivado\*\* (óvalo punteado), calculado a partir de `startTime` y `AppointmentType.duration`.

&#x20;    - Atributos de trazabilidad como `status`, `cancel\_reason` y `observations`, necesarios para soportar las historias de confirmar, cancelar, completar y marcar como no asistida una cita.

