package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;          // ← nuevo

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorScheduleRepositoryTest extends AbstractRepositoryIT {

    @Autowired private DoctorScheduleRepository doctorScheduleRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        doctorScheduleRepository.deleteAll();
        doctorRepository.deleteAll();
        specialityRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Doctor savedDoctor() {
        Speciality spec = specialityRepository.save(Speciality.builder().name("Neurology").build());
        return doctorRepository.save(Doctor.builder()
                .fullName("Dr. Neuro")
                .documentType("CC")
                .documentNumber("DOC789")
                .email("neuro@test.com")
                .registerNum("REG456")
                .speciality(spec)
                .build());
    }

    private DoctorSchedule savedSchedule(Doctor doctor, WeekDay weekDay,
                                          LocalTime start, LocalTime end) {
        return doctorScheduleRepository.save(DoctorSchedule.builder()
                .weekDay(weekDay)              // ← enum, no String
                .startTime(start)
                .endTime(end)
                .doctor(doctor)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByDoctor_IdPerson ────────────────────────────────

    @Test
    @DisplayName("Encuentra horarios por id de doctor")
    void shouldFindSchedulesByDoctorId() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        savedSchedule(doctor, WeekDay.TUESDAY, LocalTime.of(14, 0), LocalTime.of(18, 0));

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository.findByDoctor_IdPerson(doctor.getIdPerson());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getDoctor().getIdPerson().equals(doctor.getIdPerson()));
    }

    // ── findByWeekDay ────────────────────────────────────────

    @Test
    @DisplayName("Encuentra horarios por día de la semana")
    void shouldFindSchedulesByWeekDay() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(14, 0), LocalTime.of(18, 0));
        savedSchedule(doctor, WeekDay.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository.findByWeekDay(WeekDay.MONDAY);  // ← enum

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getWeekDay() == WeekDay.MONDAY);
    }

    @Test
    @DisplayName("Retorna lista vacía si no hay horarios en ese día")
    void shouldReturnEmptyWhenNoSchedulesForWeekDay() {
        // Arrange — BD vacía

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository.findByWeekDay(WeekDay.SUNDAY);

        // Assert
        assertThat(result).isEmpty();
    }

    // ── findByStatus ─────────────────────────────────────────

    @Test
    @DisplayName("Encuentra horarios por status")
    void shouldFindSchedulesByStatus() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));  // AVAILABLE por defecto

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .weekDay(WeekDay.TUESDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .status(ScheduleStatus.BLOCKED)
                .doctor(doctor)
                .build());

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository.findByStatus(ScheduleStatus.AVAILABLE);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ScheduleStatus.AVAILABLE);
    }

    // ── findByDoctor_IdPersonAndWeekDay ──────────────────────

    @Test
    @DisplayName("Encuentra horarios de un doctor en un día específico")
    void shouldFindSchedulesByDoctorAndWeekDay() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        savedSchedule(doctor, WeekDay.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository
                .findByDoctor_IdPersonAndWeekDay(doctor.getIdPerson(), WeekDay.MONDAY);  // ← enum

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWeekDay()).isEqualTo(WeekDay.MONDAY);
    }

    // ── existsScheduleConflict ───────────────────────────────

    @Test
    @DisplayName("Detecta conflicto de horario cuando hay solapamiento")
    void shouldDetectScheduleConflict() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        // Act — intenta crear horario que se solapa
        boolean conflict = doctorScheduleRepository.existsScheduleConflict(
                doctor.getIdPerson(),
                WeekDay.MONDAY,
                LocalTime.of(10, 0),   // se solapa con 8-12
                LocalTime.of(14, 0)
        );

        // Assert
        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("No detecta conflicto cuando no hay solapamiento")
    void shouldNotDetectConflictWhenNoOverlap() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        // Act — horario sin solapamiento
        boolean conflict = doctorScheduleRepository.existsScheduleConflict(
                doctor.getIdPerson(),
                WeekDay.MONDAY,
                LocalTime.of(13, 0),   // después de 12:00, no hay solapamiento
                LocalTime.of(17, 0)
        );

        // Assert
        assertThat(conflict).isFalse();
    }

    // ── findActiveSchedulesByDoctor ──────────────────────────

    @Test
    @DisplayName("Encuentra horarios disponibles de un doctor ordenados")
    void shouldFindActiveSchedulesByDoctor() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.FRIDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .weekDay(WeekDay.WEDNESDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .status(ScheduleStatus.BLOCKED)   // no debe aparecer
                .doctor(doctor)
                .build());

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository
                .findActiveSchedulesByDoctor(doctor.getIdPerson());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getStatus() == ScheduleStatus.AVAILABLE);
    }

    // ── findActiveSchedulesByWeekDay ─────────────────────────

    @Test
    @DisplayName("Encuentra horarios disponibles por día con doctor cargado")
    void shouldFindActiveSchedulesByWeekDay() {
        // Arrange
        Doctor doctor = savedDoctor();
        savedSchedule(doctor, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));

        doctorScheduleRepository.save(DoctorSchedule.builder()
                .weekDay(WeekDay.MONDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .status(ScheduleStatus.BLOCKED)   // no debe aparecer
                .doctor(doctor)
                .build());

        // Act
        List<DoctorSchedule> result = doctorScheduleRepository
                .findActiveSchedulesByWeekDay(WeekDay.MONDAY);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDoctor()).isNotNull();  // JOIN FETCH funcionó
        assertThat(result.get(0).getStatus()).isEqualTo(ScheduleStatus.AVAILABLE);
    }
}