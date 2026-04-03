package unimag.proyect.repositories;

import unimag.proyect.entities.Doctor;
import unimag.proyect.enums.PersonStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import unimag.proyect.enums.WeekDay;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    // Query Methods
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByRegisterNum(String registerNum);
    List<Doctor> findByFullNameContainingIgnoreCase(String name);
    List<Doctor> findByStatus(PersonStatus status);
    List<Doctor> findBySpeciality_Name(String specialityName);

    // Doctor con sus horarios cargados (evita N+1)
    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.schedules WHERE d.idPerson = :id")
    Optional<Doctor> findByIdWithSchedules(@Param("id") UUID id);

    // Doctores disponibles en un día y rango horario específico
    @Query("""
            SELECT DISTINCT d FROM Doctor d
            JOIN d.schedules s
            WHERE s.weekDay = :weekDay
            AND s.startTime <= :startTime
            AND s.endTime >= :endTime
            AND s.status = unimag.proyect.enums.ScheduleStatus.AVAILABLE
            """)
    List<Doctor> findAvailableDoctors(
            @Param("weekDay") WeekDay weekDay,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Doctores por especialidad con sus horarios
    @Query("SELECT DISTINCT d FROM Doctor d JOIN FETCH d.schedules s WHERE d.speciality.idSpeciality = :specialityId")
    List<Doctor> findBySpecialityWithSchedules(@Param("specialityId") UUID specialityId);
}