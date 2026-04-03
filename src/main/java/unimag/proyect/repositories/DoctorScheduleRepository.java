package unimag.proyect.repositories;

import unimag.proyect.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    // Query Methods
    List<DoctorSchedule> findByDoctor_IdPerson(UUID doctorId);
    List<DoctorSchedule> findByWeekDay(WeekDay weekDay);
    List<DoctorSchedule> findByStatus(ScheduleStatus status);
    List<DoctorSchedule> findByDoctor_IdPersonAndWeekDay(UUID doctorId, WeekDay weekDay);

    // Verificar si un doctor ya tiene horario en ese día y hora (evita duplicados)
    @Query("""
            SELECT COUNT(s) > 0 FROM DoctorSchedule s
            WHERE s.doctor.idPerson = :doctorId
            AND s.weekDay = :weekDay
            AND s.status = unimag.proyect.enums.ScheduleStatus.AVAILABLE
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    boolean existsScheduleConflict(
            @Param("doctorId") UUID doctorId,
            @Param("weekDay")  WeekDay weekDay,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Horarios activos de un doctor ordenados por día y hora
    @Query("""
            SELECT s FROM DoctorSchedule s
            WHERE s.doctor.idPerson = :doctorId
            AND s.status = unimag.proyect.enums.ScheduleStatus.AVAILABLE
            ORDER BY s.weekDay ASC, s.startTime ASC
            """)
    List<DoctorSchedule> findActiveSchedulesByDoctor(@Param("doctorId") UUID doctorId);

    // Todos los horarios de un día específico con sus doctores cargados
    @Query("""
            SELECT s FROM DoctorSchedule s
            JOIN FETCH s.doctor
            WHERE s.weekDay = :weekDay
            AND s.status = unimag.proyect.enums.ScheduleStatus.AVAILABLE
            """)
    List<DoctorSchedule> findActiveSchedulesByWeekDay(@Param("weekDay") WeekDay weekDay);
}