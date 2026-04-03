package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorRepositoryTest extends AbstractRepositoryIT {

    @Autowired private DoctorRepository doctorRepository;
    @Autowired private SpecialityRepository specialityRepository;
    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void clean() {
        doctorScheduleRepository.deleteAll();
        doctorRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Speciality savedSpeciality(String name) {
        return specialityRepository.save(Speciality.builder().name(name).build());
    }

    private Doctor savedDoctor(String docNum, String email, String regNum, Speciality spec) {
        return doctorRepository.save(Doctor.builder()
                .fullName("Doc Test")
                .documentType("CC")
                .documentNumber(docNum)
                .email(email)
                .registerNum(regNum)
                .speciality(spec)
                .build());
    }

    private DoctorSchedule savedSchedule(Doctor doctor, WeekDay weekDay,
                                          LocalTime start, LocalTime end,
                                          ScheduleStatus status) {
        return doctorScheduleRepository.save(DoctorSchedule.builder()
                .weekDay(weekDay)
                .startTime(start)
                .endTime(end)
                .status(status)
                .doctor(doctor)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByEmail ──────────────────────────────────────────

    @Test
    @DisplayName("Encuentra doctor por email")
    void shouldFindDoctorByEmail() {
        Speciality spec = savedSpeciality("Pediatrics");
        savedDoctor("DOC001", "doctor1@test.com", "REG001", spec);

        Optional<Doctor> result = doctorRepository.findByEmail("doctor1@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("doctor1@test.com");
    }

    @Test
    @DisplayName("Retorna vacío si el email no existe")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<Doctor> result = doctorRepository.findByEmail("noexiste@test.com");

        assertThat(result).isEmpty();
    }

    // ── findByRegisterNum ────────────────────────────────────

    @Test
    @DisplayName("Encuentra doctor por número de registro")
    void shouldFindDoctorByRegisterNum() {
        Speciality spec = savedSpeciality("Cardiology");
        savedDoctor("DOC002", "doctor2@test.com", "REG123", spec);

        Optional<Doctor> result = doctorRepository.findByRegisterNum("REG123");

        assertThat(result).isPresent();
        assertThat(result.get().getDocumentNumber()).isEqualTo("DOC002");
    }

    @Test
    @DisplayName("Retorna vacío si el número de registro no existe")
    void shouldReturnEmptyWhenRegisterNumNotFound() {
        Optional<Doctor> result = doctorRepository.findByRegisterNum("NOEXISTE");

        assertThat(result).isEmpty();
    }

    // ── findByFullNameContainingIgnoreCase ───────────────────

    @Test
    @DisplayName("Encuentra doctores por nombre parcial")
    void shouldFindDoctorsByPartialName() {
        // Arrange — una especialidad distinta por doctor (UNIQUE constraint)
        savedDoctor("DOC003", "carlos@test.com",  "REG003", savedSpeciality("Neurology-1"));
        savedDoctor("DOC004", "carlos2@test.com", "REG004", savedSpeciality("Neurology-2"));
        savedDoctor("DOC005", "ana@test.com",     "REG005", savedSpeciality("Neurology-3"));

        // Act
        List<Doctor> result = doctorRepository.findByFullNameContainingIgnoreCase("Doc Test");

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(d -> d.getFullName().toLowerCase().contains("doc test"));
    }

    @Test
    @DisplayName("Búsqueda por nombre es case-insensitive")
    void shouldFindDoctorsCaseInsensitive() {
        Speciality spec = savedSpeciality("Surgery");
        savedDoctor("DOC006", "doc6@test.com", "REG006", spec);

        List<Doctor> result = doctorRepository.findByFullNameContainingIgnoreCase("DOC TEST");

        assertThat(result).hasSize(1);
    }

    // ── findByStatus ─────────────────────────────────────────

    @Test
    @DisplayName("Encuentra doctores por status")
    void shouldFindDoctorsByStatus() {
        // Arrange — especialidades distintas por UNIQUE constraint
        Speciality specActive   = savedSpeciality("Dermatology");
        Speciality specInactive = savedSpeciality("Geriatrics");

        savedDoctor("DOC007", "doc7@test.com", "REG007", specActive); // ACTIVE por @Builder.Default

        doctorRepository.save(Doctor.builder()
                .fullName("Inactive Doctor")
                .documentType("CC")
                .documentNumber("DOC008")
                .email("doc8@test.com")
                .registerNum("REG008")
                .speciality(specInactive)              // ← especialidad distinta
                .status(PersonStatus.INACTIVE)
                .build());

        // Act
        List<Doctor> result = doctorRepository.findByStatus(PersonStatus.ACTIVE);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentNumber()).isEqualTo("DOC007");
    }

    // ── findBySpeciality_Name ────────────────────────────────

    @Test
    @DisplayName("Encuentra doctor por nombre de especialidad")
    void shouldFindDoctorsBySpecialityName() {
        // Arrange — UNIQUE(id_speciality): cada especialidad tiene exactamente 1 doctor
        savedDoctor("DOC009", "doc9@test.com",  "REG009", savedSpeciality("Cardiology"));
        savedDoctor("DOC010", "doc10@test.com", "REG010", savedSpeciality("Neurology"));

        // Act
        List<Doctor> result = doctorRepository.findBySpeciality_Name("Cardiology");

        // Assert — solo puede haber 1 doctor por especialidad (constraint de BD)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpeciality().getName()).isEqualTo("Cardiology");
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay doctores con esa especialidad")
    void shouldReturnEmptyWhenNoDoctoWithSpeciality() {
        List<Doctor> result = doctorRepository.findBySpeciality_Name("Unknown");

        assertThat(result).isEmpty();
    }

    // ── findByIdWithSchedules ────────────────────────────────

    @Test
    @Transactional
    void shouldFindDoctorByIdWithSchedules() {
        Speciality spec = savedSpeciality("Oncology");
        Doctor doctor = savedDoctor("DOC011", "doc11@test.com", "REG011", spec);
        savedSchedule(doctor, WeekDay.MONDAY,  LocalTime.of(8,  0), LocalTime.of(12, 0), ScheduleStatus.AVAILABLE);
        savedSchedule(doctor, WeekDay.FRIDAY,  LocalTime.of(14, 0), LocalTime.of(18, 0), ScheduleStatus.AVAILABLE);

        entityManager.flush();   // ← persiste los cambios pendientes en la sesión
        entityManager.clear();   // ← limpia L1 cache, fuerza recarga real

        Optional<Doctor> result = doctorRepository.findByIdWithSchedules(doctor.getIdPerson());
        assertThat(result).isPresent();
        assertThat(result.get().getSchedules()).hasSize(2);
    }

    // ── findAvailableDoctors ─────────────────────────────────

    @Test
    @DisplayName("Encuentra doctores disponibles en día y horario específico")
    void shouldFindAvailableDoctors() {
        // Arrange — especialidades distintas por UNIQUE constraint
        Doctor available   = savedDoctor("DOC012", "doc12@test.com", "REG012", savedSpeciality("Radiology"));
        Doctor unavailable = savedDoctor("DOC013", "doc13@test.com", "REG013", savedSpeciality("Pathology"));

        savedSchedule(available,   WeekDay.MONDAY, LocalTime.of(7, 0), LocalTime.of(13, 0), ScheduleStatus.AVAILABLE);
        savedSchedule(unavailable, WeekDay.MONDAY, LocalTime.of(7, 0), LocalTime.of(13, 0), ScheduleStatus.UNAVAILABLE);

        // Act — busca disponibles el lunes entre 8 y 12
        List<Doctor> result = doctorRepository.findAvailableDoctors(
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdPerson()).isEqualTo(available.getIdPerson());
    }

    @Test
    @DisplayName("Retorna vacío si no hay doctores disponibles en ese horario")
    void shouldReturnEmptyWhenNoDoctorsAvailable() {
        List<Doctor> result = doctorRepository.findAvailableDoctors(
                WeekDay.TUESDAY,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0)
        );

        assertThat(result).isEmpty();
    }

    // ── findBySpecialityWithSchedules ────────────────────────

    @Test
    @Transactional
        void shouldFindBySpecialityWithSchedules() {
        Speciality spec = savedSpeciality("Psychiatry");
        Doctor doctor = savedDoctor("DOC014", "doc14@test.com", "REG014", spec);
        savedSchedule(doctor, WeekDay.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), ScheduleStatus.AVAILABLE);

        entityManager.flush();
        entityManager.clear();

        List<Doctor> result = doctorRepository.findBySpecialityWithSchedules(spec.getIdSpeciality());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSchedules()).isNotEmpty();
    }
}