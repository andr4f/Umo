package unimag.proyect.repositories;

import unimag.proyect.entities.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialityRepository extends JpaRepository<Speciality, UUID> {

    // Buscar por nombre exacto
    Optional<Speciality> findByName(String name);

    // Buscar por nombre parcial — útil para buscador en UI
    List<Speciality> findByNameContainingIgnoreCase(String name);

    // Verificar si existe
    boolean existsByName(String name);

    // Especialidades que tienen al menos un doctor activo
    @Query("""
            SELECT DISTINCT s FROM Speciality s
            JOIN s.doctors d
            WHERE d.status = 'ACTIVE'
            """)
    List<Speciality> findSpecialitiesWithActiveDoctors();
}