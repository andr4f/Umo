package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.*;
import unimag.proyect.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentRepositoryTest extends AbstractRepositoryIT {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private OfficeRepository officeRepository;
    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        officeRepository.deleteAll();
        appointmentTypeRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Patient savedPatient(String docNum, String email) {
        return patientRepository.save(Patient.builder()
                .fullName("Pat Test")
                .documentType("CC")
                .documentNumber(docNum)
                .email(email)
                .phone("1234567890")
                .build());
    }

    private Doctor savedDoctor(String docNum, String email, String regNum) {
        Speciality spec = specialityRepository.save(
                Speciality.builder().name("General-" + regNum).build());
        return doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC")
                .documentNumber(docNum)
                .email(email)
                .registerNum(regNum)
                .speciality(spec)
                .build());
    }

    private Office savedOffice(String code) {
        return officeRepository.save(Office.builder()
                .code(code)
                .name("Office " + code)
                .location("Building A")
                .build());
    }

    private AppointmentType savedType() {
        return appointmentTypeRepository.save(
                AppointmentType.builder().name("General Checkup").duration(30).build());
    }

    private Appointment savedAppointment(Patient patient, Doctor doctor, Office office,
                                          AppointmentType type, AppointmentStatus status,
                                          LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .office(office)
                .appointmentType(type)
                .status(status)                    // ← enum, no String
                .startTime(start)
                .endTime(end)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByStatus ─────────────────────────────────────────

    @Test
    @DisplayName("Encuentra citas por status")
    void shouldFindAppointmentsByStatus() {
        // Arrange
        Patient patient = savedPatient("P001", "p1@test.com");
        Doctor doctor   = savedDoctor("D001", "d1@test.com", "R001");
        Office office   = savedOffice("OFC-01");
        AppointmentType type = savedType();

        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 4, 10, 10, 0);

        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED, start, end);
        savedAppointment(patient, doctor, office, type, AppointmentStatus.CANCELLED,
                start.plusDays(1), end.plusDays(1));

        // Act
        List<Appointment> result = appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    // ── findByPatient_IdPerson ────────────────────────────────

    @Test
    @DisplayName("Encuentra citas por id de paciente")
    void shouldFindAppointmentsByPatientId() {
        // Arrange
        Patient patient1 = savedPatient("P002", "p2@test.com");
        Patient patient2 = savedPatient("P003", "p3@test.com");
        Doctor doctor    = savedDoctor("D002", "d2@test.com", "R002");
        Office office    = savedOffice("OFC-02");
        AppointmentType type = savedType();

        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);
        savedAppointment(patient1, doctor, office, type, AppointmentStatus.SCHEDULED, start, start.plusHours(1));
        savedAppointment(patient1, doctor, office, type, AppointmentStatus.COMPLETED, start.plusDays(1), start.plusDays(1).plusHours(1));
        savedAppointment(patient2, doctor, office, type, AppointmentStatus.SCHEDULED, start.plusDays(2), start.plusDays(2).plusHours(1));

        // Act
        List<Appointment> result = appointmentRepository.findByPatient_IdPerson(patient1.getIdPerson());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getPatient().getIdPerson().equals(patient1.getIdPerson()));
    }

    // ── findByDoctor_IdPerson ────────────────────────────────

    @Test
    @DisplayName("Encuentra citas por id de doctor")
    void shouldFindAppointmentsByDoctorId() {
        // Arrange
        Patient patient = savedPatient("P004", "p4@test.com");
        Doctor doctor1  = savedDoctor("D003", "d3@test.com", "R003");
        Doctor doctor2  = savedDoctor("D004", "d4@test.com", "R004");
        Office office   = savedOffice("OFC-03");
        AppointmentType type = savedType();

        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);
        savedAppointment(patient, doctor1, office, type, AppointmentStatus.SCHEDULED, start, start.plusHours(1));
        savedAppointment(patient, doctor2, office, type, AppointmentStatus.SCHEDULED, start.plusDays(1), start.plusDays(1).plusHours(1));

        // Act
        List<Appointment> result = appointmentRepository.findByDoctor_IdPerson(doctor1.getIdPerson());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctor().getIdPerson()).isEqualTo(doctor1.getIdPerson());
    }

    // ── findByOffice_IdOffice ────────────────────────────────

    @Test
    @DisplayName("Encuentra citas por id de consultorio")
    void shouldFindAppointmentsByOfficeId() {
        // Arrange
        Patient patient = savedPatient("P005", "p5@test.com");
        Doctor doctor   = savedDoctor("D005", "d5@test.com", "R005");
        Office office1  = savedOffice("OFC-04");
        Office office2  = savedOffice("OFC-05");
        AppointmentType type = savedType();

        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);
        savedAppointment(patient, doctor, office1, type, AppointmentStatus.SCHEDULED, start, start.plusHours(1));
        savedAppointment(patient, doctor, office2, type, AppointmentStatus.SCHEDULED, start.plusDays(1), start.plusDays(1).plusHours(1));

        // Act
        List<Appointment> result = appointmentRepository.findByOffice_IdOffice(office1.getIdOffice());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOffice().getIdOffice()).isEqualTo(office1.getIdOffice());
    }

    // ── findByIdWithDetails ──────────────────────────────────

    @Test
    @DisplayName("Carga cita con todas sus relaciones (evita N+1)")
    void shouldFindAppointmentByIdWithDetails() {
        // Arrange
        Patient patient = savedPatient("P006", "p6@test.com");
        Doctor doctor   = savedDoctor("D006", "d6@test.com", "R006");
        Office office   = savedOffice("OFC-06");
        AppointmentType type = savedType();

        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);
        Appointment appt = savedAppointment(patient, doctor, office, type,
                AppointmentStatus.SCHEDULED, start, start.plusHours(1));

        // Act
        Optional<Appointment> result = appointmentRepository.findByIdWithDetails(appt.getIdAppointment());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPatient()).isNotNull();
        assertThat(result.get().getDoctor()).isNotNull();
        assertThat(result.get().getOffice()).isNotNull();
        assertThat(result.get().getAppointmentType()).isNotNull();
    }

    // ── findByDoctorAndDateRange ─────────────────────────────

    @Test
    @DisplayName("Encuentra citas de un doctor en un rango de fechas")
    void shouldFindAppointmentsByDoctorAndDateRange() {
        // Arrange
        Patient patient = savedPatient("P007", "p7@test.com");
        Doctor doctor   = savedDoctor("D007", "d7@test.com", "R007");
        Office office   = savedOffice("OFC-07");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));
        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 11, 9, 0), LocalDateTime.of(2026, 4, 11, 10, 0));
        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 20, 9, 0), LocalDateTime.of(2026, 4, 20, 10, 0)); // fuera del rango

        // Act
        List<Appointment> result = appointmentRepository.findByDoctorAndDateRange(
                doctor.getIdPerson(),
                LocalDateTime.of(2026, 4, 10, 0, 0),
                LocalDateTime.of(2026, 4, 12, 0, 0)
        );

        // Assert
        assertThat(result).hasSize(2);
    }

    // ── existsDoctorConflict ─────────────────────────────────

    @Test
    @DisplayName("Detecta conflicto de horario para un doctor")
    void shouldDetectDoctorConflict() {
        // Arrange
        Patient patient = savedPatient("P008", "p8@test.com");
        Doctor doctor   = savedDoctor("D008", "d8@test.com", "R008");
        Office office   = savedOffice("OFC-08");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));

        // Act — intenta agendar en horario solapado
        boolean conflict = appointmentRepository.existsDoctorConflict(
                doctor.getIdPerson(),
                LocalDateTime.of(2026, 4, 10, 9, 30),
                LocalDateTime.of(2026, 4, 10, 11, 0)
        );

        // Assert
        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("No detecta conflicto para doctor con cita CANCELLED")
    void shouldNotDetectDoctorConflictWhenCancelled() {
        // Arrange
        Patient patient = savedPatient("P009", "p9@test.com");
        Doctor doctor   = savedDoctor("D009", "d9@test.com", "R009");
        Office office   = savedOffice("OFC-09");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.CANCELLED,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));

        // Act
        boolean conflict = appointmentRepository.existsDoctorConflict(
                doctor.getIdPerson(),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );

        // Assert — cita cancelada no genera conflicto
        assertThat(conflict).isFalse();
    }

    // ── existsOfficeConflict ─────────────────────────────────

    @Test
    @DisplayName("Detecta conflicto de horario en consultorio")
    void shouldDetectOfficeConflict() {
        // Arrange
        Patient patient = savedPatient("P010", "p10@test.com");
        Doctor doctor   = savedDoctor("D010", "d10@test.com", "R010");
        Office office   = savedOffice("OFC-10");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));

        // Act
        boolean conflict = appointmentRepository.existsOfficeConflict(
                office.getIdOffice(),
                LocalDateTime.of(2026, 4, 10, 9, 30),
                LocalDateTime.of(2026, 4, 10, 11, 0)
        );

        // Assert
        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("No detecta conflicto en consultorio con cita NO_SHOW")
    void shouldNotDetectOfficeConflictWhenNoShow() {
        // Arrange
        Patient patient = savedPatient("P011", "p11@test.com");
        Doctor doctor   = savedDoctor("D011", "d11@test.com", "R011");
        Office office   = savedOffice("OFC-11");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.NO_SHOW,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));

        // Act
        boolean conflict = appointmentRepository.existsOfficeConflict(
                office.getIdOffice(),
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );

        // Assert — NO_SHOW no genera conflicto
        assertThat(conflict).isFalse();
    }

    // ── findPatientHistory ───────────────────────────────────

    @Test
    @DisplayName("Retorna historial de citas de un paciente ordenado por fecha descendente")
    void shouldFindPatientHistoryOrderedByDate() {
        // Arrange
        Patient patient = savedPatient("P012", "p12@test.com");
        Doctor doctor   = savedDoctor("D012", "d12@test.com", "R012");
        Office office   = savedOffice("OFC-12");
        AppointmentType type = savedType();

        savedAppointment(patient, doctor, office, type, AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 8, 9, 0), LocalDateTime.of(2026, 4, 8, 10, 0));
        savedAppointment(patient, doctor, office, type, AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 10, 9, 0), LocalDateTime.of(2026, 4, 10, 10, 0));
        savedAppointment(patient, doctor, office, type, AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 4, 15, 9, 0), LocalDateTime.of(2026, 4, 15, 10, 0));

        // Act
        List<Appointment> result = appointmentRepository.findPatientHistory(patient.getIdPerson());

        // Assert — orden descendente: la más reciente primero
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStartTime())
                .isAfter(result.get(1).getStartTime());
        assertThat(result.get(1).getStartTime())
                .isAfter(result.get(2).getStartTime());
    }

    @Test
    @DisplayName("Retorna lista vacía si el paciente no tiene historial")
    void shouldReturnEmptyWhenPatientHasNoHistory() {
        // Arrange
        Patient patient = savedPatient("P013", "p13@test.com");

        // Act
        List<Appointment> result = appointmentRepository.findPatientHistory(patient.getIdPerson());

        // Assert
        assertThat(result).isEmpty();
    }
}