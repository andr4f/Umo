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

class OfficeRepositoryTest extends AbstractRepositoryIT {

    @Autowired private OfficeRepository          officeRepository;
    @Autowired private AppointmentRepository     appointmentRepository;
    @Autowired private DoctorRepository          doctorRepository;
    @Autowired private PatientRepository         patientRepository;
    @Autowired private SpecialityRepository      specialityRepository;    // ← agregar
    @Autowired private AppointmentTypeRepository appointmentTypeRepository; // ← agregar

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();    // primero las FK hijas
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        officeRepository.deleteAll();
        specialityRepository.deleteAll();
        appointmentTypeRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Office savedOffice(String code, String name) {
        return officeRepository.save(Office.builder()
                .code(code)
                .name(name)
                .location("Building A")
                .status(OfficeStatus.ACTIVE)   // ← explícito, no depender de @Builder.Default
                .build());
    }

    private Doctor savedDoctor() {
        Speciality spec = specialityRepository.save(
                Speciality.builder()
                        .name("Spec-" + UUID.randomUUID())
                        .build());
        return doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC")
                .documentNumber("DOC-" + UUID.randomUUID())
                .email("doc-" + UUID.randomUUID() + "@test.com")
                .registerNum("REG-" + UUID.randomUUID())
                .speciality(spec)
                .build());
    }

    private Patient savedPatient() {
        return patientRepository.save(Patient.builder()
                .fullName("Pat Test")
                .documentType("CC")
                .documentNumber("PAT-" + UUID.randomUUID())
                .email("pat-" + UUID.randomUUID() + "@test.com")
                .phone("3001234567")
                .build());
    }

    private AppointmentType savedType() {
        return appointmentTypeRepository.save(AppointmentType.builder()
                .name("Type-" + UUID.randomUUID())
                .duration(30)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByCode ───────────────────────────────────────────

    @Test
    @DisplayName("Encuentra consultorio por código exacto")
    void shouldFindOfficeByCode() {
        savedOffice("OFC-01", "Main Office");

        Optional<Office> result = officeRepository.findByCode("OFC-01");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Main Office");
    }

    @Test
    @DisplayName("Retorna vacío si el código no existe")
    void shouldReturnEmptyWhenCodeNotFound() {
        Optional<Office> result = officeRepository.findByCode("OFC-99");

        assertThat(result).isEmpty();
    }

    // ── findByName ───────────────────────────────────────────

    @Test
    @DisplayName("Encuentra consultorio por nombre")
    void shouldFindOfficeByName() {
        savedOffice("OFC-02", "Cardiology Room");

        Optional<Office> result = officeRepository.findByName("Cardiology Room");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("OFC-02");
    }

    @Test
    @DisplayName("Retorna vacío si el nombre no existe")
    void shouldReturnEmptyWhenNameNotFound() {
        Optional<Office> result = officeRepository.findByName("Ghost Room");

        assertThat(result).isEmpty();
    }

    // ── findByStatus ─────────────────────────────────────────

    @Test
    @DisplayName("Encuentra consultorios por status")
    void shouldFindOfficesByStatus() {
        savedOffice("OFC-03", "Active Office");  // ACTIVE explícito en helper

        officeRepository.save(Office.builder()
                .code("OFC-04")
                .name("Maintenance Office")
                .location("Building B")
                .status(OfficeStatus.UNDER_MAINTENANCE)
                .build());

        List<Office> result = officeRepository.findByStatus(OfficeStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("OFC-03");
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay consultorios con ese status")
    void shouldReturnEmptyWhenNoOfficeWithStatus() {
        savedOffice("OFC-05", "Active Office");

        List<Office> result = officeRepository.findByStatus(OfficeStatus.UNDER_MAINTENANCE);

        assertThat(result).isEmpty();
    }

    // ── existsByCode ─────────────────────────────────────────

    @Test
    @DisplayName("Retorna true si el código de consultorio existe")
    void shouldReturnTrueWhenCodeExists() {
        savedOffice("OFC-06", "Some Office");

        assertThat(officeRepository.existsByCode("OFC-06")).isTrue();
    }

    @Test
    @DisplayName("Retorna false si el código de consultorio no existe")
    void shouldReturnFalseWhenCodeNotExists() {
        assertThat(officeRepository.existsByCode("OFC-99")).isFalse();
    }

    // ── findAvailableOffices ─────────────────────────────────

    @Test
    @DisplayName("Retorna consultorios sin citas activas en el rango horario")
    void shouldFindAvailableOffices() {
        Office freeOffice = savedOffice("OFC-07", "Free Office");
        Office busyOffice = savedOffice("OFC-08", "Busy Office");

        Doctor doctor      = savedDoctor();
        Patient patient    = savedPatient();
        AppointmentType type = savedType();

        appointmentRepository.save(Appointment.builder()
                .office(busyOffice)
                .doctor(doctor)
                .patient(patient)
                .appointmentType(type)
                .status(AppointmentStatus.SCHEDULED)
                .startTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 4, 10, 10, 0))
                .build());

        List<Office> result = officeRepository.findAvailableOffices(
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("OFC-07");
    }

    @Test
    @DisplayName("Consultorio con cita CANCELLED sigue disponible")
    void shouldShowOfficeFreeWhenAppointmentCancelled() {
        Office office        = savedOffice("OFC-09", "Office with cancelled appt");
        Doctor doctor        = savedDoctor();
        Patient patient      = savedPatient();
        AppointmentType type = savedType();

        appointmentRepository.save(Appointment.builder()
                .office(office)
                .doctor(doctor)
                .patient(patient)
                .appointmentType(type)
                .status(AppointmentStatus.CANCELLED)   // cancelada → no bloquea
                .startTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 4, 10, 10, 0))
                .build());

        List<Office> result = officeRepository.findAvailableOffices(
                LocalDateTime.of(2026, 4, 10, 9, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("OFC-09");
    }
}