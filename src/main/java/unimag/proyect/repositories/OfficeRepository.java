package unimag.proyect.repositories;

import unimag.proyect.entities.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfficeRepository extends JpaRepository<Office, UUID> {

    // Query Methods
    Optional<Office> findByCode(String code);
    Optional<Office> findByName(String name);
    List<Office> findByStatus(String status);
    boolean existsByCode(String code);

    // Consultorios disponibles en un rango horario específico
    @Query("""
            SELECT o FROM Office o
            WHERE o.status = 'ACTIVE'
            AND o.idOffice NOT IN (
                SELECT a.office.idOffice FROM Appointment a
                WHERE a.status NOT IN ('CANCELLED', 'NO_SHOW')
                AND a.startTime < :endTime
                AND a.endTime > :startTime
            )
            """)
    List<Office> findAvailableOffices(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}