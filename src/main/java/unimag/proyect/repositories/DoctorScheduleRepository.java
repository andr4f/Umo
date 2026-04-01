package unimag.proyect.repositories;

import unimag.proyect.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    // Query Methods
    List<DoctorSchedule> findByDoctor_IdPerson(UUID doctorId);
    List<DoctorSchedule> findByWeekDay(String weekDay);
    List<DoctorSchedule> findByStatus(String status);
    List<DoctorSchedule> findByDoctor_IdPersonAndWeekDay(UUID doctorId, String weekDay);

    // Verificar si un doctor ya tiene horario en ese día y hora (evita duplicados)
    @Query("""
            SELECT COUNT(s) > 0 FROM DoctorSchedule s
            WHERE s.doctor.idPerson = :doctorId
            AND s.weekDay = :weekDay
            AND s.status = 'ACTIVE'
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            """)
    boolean existsScheduleConflict(
            @Param("doctorId") UUID doctorId,
            @Param("weekDay") String weekDay,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Horarios activos de un doctor ordenados por día y hora
    @Query("""
            SELECT s FROM DoctorSchedule s
            WHERE s.doctor.idPerson = :doctorId
            AND s.status = 'ACTIVE'
            ORDER BY s.weekDay ASC, s.startTime ASC
            """)
    List<DoctorSchedule> findActiveSchedulesByDoctor(@Param("doctorId") UUID doctorId);

    // Todos los horarios de un día específico con sus doctores cargados
    @Query("""
            SELECT s FROM DoctorSchedule s
            JOIN FETCH s.doctor
            WHERE s.weekDay = :weekDay
            AND s.status = 'ACTIVE'
            """)
    List<DoctorSchedule> findActiveSchedulesByWeekDay(@Param("weekDay") String weekDay);
}