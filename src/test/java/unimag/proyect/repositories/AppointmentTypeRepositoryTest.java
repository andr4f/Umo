package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Appointment;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Office;
import unimag.proyect.entities.Patient;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.enums.OfficeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentTypeRepositoryTest extends AbstractRepositoryIT {

    @Autowired private AppointmentTypeRepository appointmentTypeRepository;
    @Autowired private AppointmentRepository     appointmentRepository;
    @Autowired private DoctorRepository          doctorRepository;
    @Autowired private PatientRepository         patientRepository;
    @Autowired private OfficeRepository          officeRepository;
    @Autowired private SpecialityRepository      specialityRepository;   // ← agregar

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        appointmentTypeRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        officeRepository.deleteAll();
        specialityRepository.deleteAll();                                // ← agregar
    }

    // ── helpers ──────────────────────────────────────────────
    private AppointmentType savedType(String name, int duration) {
        return appointmentTypeRepository.save(AppointmentType.builder()
                .name(name)
                .duration(duration)
                .build());
    }

    private Appointment savedAppointment(AppointmentType type, AppointmentStatus status) {
        Speciality spec = specialityRepository.save(
                Speciality.builder()
                        .name("Spec-" + UUID.randomUUID())
                        .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC")
                .documentNumber("DOC-" + UUID.randomUUID())
                .email("doc-" + UUID.randomUUID() + "@test.com")
                .registerNum("REG-" + UUID.randomUUID())
                .speciality(spec)
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .fullName("Pat Test")
                .documentType("CC")
                .documentNumber("PAT-" + UUID.randomUUID())
                .email("pat-" + UUID.randomUUID() + "@test.com")
                .phone("3001234567")
                .build());

        Office office = officeRepository.save(Office.builder()
                .code("OFC-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Office Test")
                .location("Building A")
                .status(OfficeStatus.ACTIVE)
                .build());

        return appointmentRepository.save(Appointment.builder()
                .appointmentType(type)
                .doctor(doctor)
                .patient(patient)
                .office(office)
                .status(status)
                .startTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 4, 10, 10, 0))
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByName ───────────────────────────────────────────

    @Test
    @DisplayName("Encuentra tipo de cita por nombre exacto")
    void shouldFindAppointmentTypeByName() {
        savedType("General Checkup", 30);

        Optional<AppointmentType> result = appointmentTypeRepository.findByName("General Checkup");

        assertThat(result).isPresent();
        assertThat(result.get().getDuration()).isEqualTo(30);
    }

    @Test
    @DisplayName("Retorna vacío si el nombre no existe")
    void shouldReturnEmptyWhenNameNotFound() {
        Optional<AppointmentType> result = appointmentTypeRepository.findByName("Unknown");

        assertThat(result).isEmpty();
    }

    // ── existsByName ─────────────────────────────────────────

    @Test
    @DisplayName("Retorna true si el tipo de cita existe por nombre")
    void shouldReturnTrueWhenTypeExists() {
        savedType("Follow Up", 20);

        assertThat(appointmentTypeRepository.existsByName("Follow Up")).isTrue();
    }

    @Test
    @DisplayName("Retorna false si el tipo de cita no existe")
    void shouldReturnFalseWhenTypeNotExists() {
        assertThat(appointmentTypeRepository.existsByName("Ghost Type")).isFalse();
    }

    // ── findAllByOrderByDurationAsc ──────────────────────────

    @Test
    @DisplayName("Retorna tipos de cita ordenados por duración ascendente")
    void shouldReturnTypesOrderedByDurationAsc() {
        savedType("Long Consultation", 60);
        savedType("Quick Checkup", 15);
        savedType("General Checkup", 30);

        List<AppointmentType> result = appointmentTypeRepository.findAllByOrderByDurationAsc();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDuration()).isEqualTo(15);
        assertThat(result.get(1).getDuration()).isEqualTo(30);
        assertThat(result.get(2).getDuration()).isEqualTo(60);
    }

    // ── findByDurationLessThanEqual ──────────────────────────

    @Test
    @DisplayName("Encuentra tipos de cita con duración menor o igual al límite")
    void shouldFindTypesByMaxDuration() {
        savedType("Quick", 15);
        savedType("Normal", 30);
        savedType("Extended", 60);

        List<AppointmentType> result = appointmentTypeRepository.findByDurationLessThanEqual(30);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getDuration() <= 30);
    }

    @Test
    @DisplayName("Retorna lista vacía si ningún tipo cumple el límite de duración")
    void shouldReturnEmptyWhenNoneMatchDuration() {
        savedType("Extended", 60);

        List<AppointmentType> result = appointmentTypeRepository.findByDurationLessThanEqual(10);

        assertThat(result).isEmpty();
    }

    // ── findTypesWithAppointmentsByStatus ────────────────────

    @Test
    @DisplayName("Encuentra tipos de cita que tienen citas en estado SCHEDULED")
    void shouldFindTypesWithScheduledAppointments() {
        AppointmentType scheduled = savedType("General Checkup", 30);
        savedType("Follow Up", 20);          // sin citas — no debe aparecer

        savedAppointment(scheduled, AppointmentStatus.SCHEDULED);

        List<AppointmentType> result = appointmentTypeRepository
                .findTypesWithAppointmentsByStatus(AppointmentStatus.SCHEDULED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("General Checkup");
    }

    @Test
    @DisplayName("No retorna tipos sin citas en el estado indicado")
    void shouldNotReturnTypesWithoutMatchingStatus() {
        AppointmentType type = savedType("General Checkup", 30);
        savedAppointment(type, AppointmentStatus.CANCELLED);

        List<AppointmentType> result = appointmentTypeRepository
                .findTypesWithAppointmentsByStatus(AppointmentStatus.SCHEDULED);

        assertThat(result).isEmpty();
    }
}