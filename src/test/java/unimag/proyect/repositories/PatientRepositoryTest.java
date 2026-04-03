package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import unimag.proyect.entities.Appointment;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Office;
import unimag.proyect.entities.Patient;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.AppointmentStatus;  // ← nuevo
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.enums.PersonStatus;        // ← nuevo

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PatientRepositoryTest extends AbstractRepositoryIT {

    @Autowired private PatientRepository patientRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private SpecialityRepository specialityRepository; 
    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private OfficeRepository          officeRepository;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        officeRepository.deleteAll();
        appointmentTypeRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    private Patient savedPatient(String docNum, String email) {
        return patientRepository.save(Patient.builder()
                .fullName("Test Patient")
                .documentType("CC")
                .documentNumber(docNum)
                .email(email)
                .phone("1234567890")
                .status(PersonStatus.ACTIVE)
                .build());
    }

    private Doctor savedDoctor() {
        Speciality spec = specialityRepository.save(   // ← s minúscula
                Speciality.builder().name("Spec-" + UUID.randomUUID()).build());
        return doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC")
                .documentNumber("DOC-" + UUID.randomUUID())
                .email("doc-" + UUID.randomUUID() + "@test.com")
                .registerNum("REG-" + UUID.randomUUID())
                .speciality(spec)
                .build());
    }

    private Appointment savedAppointment(Patient patient, Doctor doctor, AppointmentStatus status) {
        AppointmentType type = appointmentTypeRepository.save(AppointmentType.builder()
                .name("Type-" + UUID.randomUUID())
                .duration(30)
                .build());

        Office office = officeRepository.save(Office.builder()
                .code("OFC-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Office Test")
                .location("Building A")
                .status(OfficeStatus.ACTIVE)
                .build());

        return appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentType(type)   // ← faltaba
                .office(office)          // ← faltaba
                .status(status)
                .startTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 4, 10, 10, 0))
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByEmail ──────────────────────────────────────────

    @Test
    @DisplayName("Encuentra un paciente por email")
    void shouldFindPatientByEmail() {
        // Arrange
        savedPatient("DOC001", "carlos@test.com");

        // Act
        Optional<Patient> result = patientRepository.findByEmail("carlos@test.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("carlos@test.com");
    }

    @Test
    @DisplayName("Retorna vacío si el email no existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<Patient> result = patientRepository.findByEmail("noexiste@test.com");
        assertThat(result).isEmpty();
    }

    // ── findByDocumentNumber ─────────────────────────────────

    @Test
    @DisplayName("Encuentra un paciente por número de documento")
    void shouldFindPatientByDocumentNumber() {
        // Arrange
        savedPatient("DOC123", "patient1@test.com");

        // Act
        Optional<Patient> result = patientRepository.findByDocumentNumber("DOC123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDocumentNumber()).isEqualTo("DOC123");
    }

    @Test
    @DisplayName("Retorna vacío si el número de documento no existe")
    void shouldReturnEmptyWhenDocumentNumberNotFound() {
        Optional<Patient> result = patientRepository.findByDocumentNumber("NOEXISTE");
        assertThat(result).isEmpty();
    }

    // ── findByFullNameContainingIgnoreCase ───────────────────

    @Test
    @DisplayName("Encuentra pacientes por nombre parcial")
    void shouldFindPatientsByPartialName() {
        // Arrange
        patientRepository.save(Patient.builder()
                .fullName("Carlos Pérez").documentType("CC")
                .documentNumber("D001").email("c1@test.com").phone("111")
                .status(PersonStatus.ACTIVE).build());    // ← enum
        patientRepository.save(Patient.builder()
                .fullName("Carlos Ramírez").documentType("CC")
                .documentNumber("D002").email("c2@test.com").phone("222")
                .status(PersonStatus.ACTIVE).build());    // ← enum
        patientRepository.save(Patient.builder()
                .fullName("Ana Gómez").documentType("CC")
                .documentNumber("D003").email("a1@test.com").phone("333")
                .status(PersonStatus.ACTIVE).build());    // ← enum

        // Act
        List<Patient> result = patientRepository.findByFullNameContainingIgnoreCase("carlos");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getFullName().toLowerCase().contains("carlos"));
    }

    @Test
    @DisplayName("Búsqueda por nombre es case-insensitive")
    void shouldFindPatientsByNameCaseInsensitive() {
        // Arrange
        patientRepository.save(Patient.builder()
                .fullName("María López").documentType("CC")
                .documentNumber("D004").email("m1@test.com").phone("444")
                .status(PersonStatus.ACTIVE).build());    // ← enum

        // Act
        List<Patient> result = patientRepository.findByFullNameContainingIgnoreCase("MARÍA");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("María López");
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay coincidencias por nombre")
    void shouldReturnEmptyListWhenNoNameMatch() {
        savedPatient("D005", "p1@test.com");
        List<Patient> result = patientRepository.findByFullNameContainingIgnoreCase("xyz");
        assertThat(result).isEmpty();
    }

    // ── findByStatus ─────────────────────────────────────────

    @Test
    @DisplayName("Encuentra pacientes por status")
    void shouldFindPatientsByStatus() {
        // Arrange
        savedPatient("D006", "active1@test.com");
        patientRepository.save(Patient.builder()
                .fullName("Inactivo Uno").documentType("CC")
                .documentNumber("D007").email("inactive@test.com").phone("555")
                .status(PersonStatus.INACTIVE).build());  // ← enum

        // Act
        List<Patient> result = patientRepository.findByStatus(PersonStatus.ACTIVE);  // ← enum

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("active1@test.com");
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay pacientes con ese status")
    void shouldReturnEmptyWhenNoPatientWithStatus() {
        savedPatient("D008", "p2@test.com");
        List<Patient> result = patientRepository.findByStatus(PersonStatus.INACTIVE);  // ← enum
        assertThat(result).isEmpty();
    }

    // ── findByIdWithAppointments ─────────────────────────────

    @Test
    @Transactional
    @DisplayName("Carga paciente con sus citas (evita N+1)")
    void shouldFindPatientByIdWithAppointments() {
        Patient patient = savedPatient("D009", "p3@test.com");
        Doctor doctor   = savedDoctor();
        savedAppointment(patient, doctor, AppointmentStatus.SCHEDULED);
        savedAppointment(patient, doctor, AppointmentStatus.CANCELLED);

        entityManager.flush();   // ← agregar
        entityManager.clear();   // ← agregar

        Optional<Patient> result = patientRepository.findByIdWithAppointments(patient.getIdPerson());
        assertThat(result).isPresent();
        assertThat(result.get().getAppointments()).hasSize(2);
    }

    @Test
    @DisplayName("Retorna vacío si el id no existe en findByIdWithAppointments")
    void shouldReturnEmptyWhenIdNotFoundWithAppointments() {
        Optional<Patient> result = patientRepository.findByIdWithAppointments(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    // ── findPatientsByDoctorId ───────────────────────────────

    @Test
    @DisplayName("Encuentra pacientes que tienen citas con un doctor específico")
    void shouldFindPatientsByDoctorId() {
        // Arrange
        Patient patient1 = savedPatient("D010", "p4@test.com");
        Patient patient2 = savedPatient("D011", "p5@test.com");
        Patient patient3 = savedPatient("D012", "p6@test.com");
        Doctor doctor1 = savedDoctor();
        Doctor doctor2 = savedDoctor();

        savedAppointment(patient1, doctor1, AppointmentStatus.SCHEDULED);  // ← enum
        savedAppointment(patient2, doctor1, AppointmentStatus.SCHEDULED);  // ← enum
        savedAppointment(patient3, doctor2, AppointmentStatus.SCHEDULED);  // ← enum

        // Act
        List<Patient> result = patientRepository.findPatientsByDoctorId(doctor1.getIdPerson());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Patient::getIdPerson)
                .containsExactlyInAnyOrder(patient1.getIdPerson(), patient2.getIdPerson());
    }

    @Test
    @DisplayName("Retorna lista vacía si el doctor no tiene pacientes")
    void shouldReturnEmptyWhenDoctorHasNoPatients() {
        Doctor doctor = savedDoctor();
        List<Patient> result = patientRepository.findPatientsByDoctorId(doctor.getIdPerson());
        assertThat(result).isEmpty();
    }

    // ── findPatientsByAppointmentStatus ──────────────────────

    @Test
    @DisplayName("Encuentra pacientes con citas en estado SCHEDULED")
    void shouldFindPatientsByAppointmentStatus() {
        // Arrange
        Patient patient1 = savedPatient("D013", "p7@test.com");
        Patient patient2 = savedPatient("D014", "p8@test.com");
        Doctor doctor = savedDoctor();

        savedAppointment(patient1, doctor, AppointmentStatus.SCHEDULED);  // ← enum
        savedAppointment(patient2, doctor, AppointmentStatus.CANCELLED);  // ← enum

        // Act
        List<Patient> result = patientRepository.findPatientsByAppointmentStatus(AppointmentStatus.SCHEDULED); // ← enum

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdPerson()).isEqualTo(patient1.getIdPerson());
    }

    @Test
    @DisplayName("No duplica pacientes con múltiples citas en el mismo estado")
    void shouldNotDuplicatePatientsWithMultipleAppointmentsSameStatus() {
        // Arrange
        Patient patient = savedPatient("D015", "p9@test.com");
        Doctor doctor = savedDoctor();

        savedAppointment(patient, doctor, AppointmentStatus.SCHEDULED);  // ← enum
        savedAppointment(patient, doctor, AppointmentStatus.SCHEDULED);  // ← enum

        // Act
        List<Patient> result = patientRepository.findPatientsByAppointmentStatus(AppointmentStatus.SCHEDULED); // ← enum

        // Assert
        assertThat(result).hasSize(1);
    }
}