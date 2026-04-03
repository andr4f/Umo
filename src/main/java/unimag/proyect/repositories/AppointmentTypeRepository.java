package unimag.proyect.repositories;

import unimag.proyect.entities.AppointmentType;
import unimag.proyect.enums.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, UUID> {

    // Query Methods
    Optional<AppointmentType> findByName(String name);
    boolean existsByName(String name);

    // Tipos de cita ordenados por duración
    List<AppointmentType> findAllByOrderByDurationAsc();

    // Tipos de cita con duración menor o igual a un límite
    List<AppointmentType> findByDurationLessThanEqual(Integer maxDuration);

    // Tipos de cita que tienen citas agendadas (útil para reportes)
    @Query("""
            SELECT DISTINCT at FROM AppointmentType at
            WHERE EXISTS (
                SELECT a FROM Appointment a
                WHERE a.appointmentType = at
                AND a.status = :status
            )
            """)
    List<AppointmentType> findTypesWithAppointmentsByStatus(@Param("status") AppointmentStatus status);
}