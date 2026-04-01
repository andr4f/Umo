package unimag.proyect.repositories;

import unimag.proyect.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Buscar por nombre — usado al asignar rol a un nuevo SystemUser
    Optional<Role> findByName(String name);

    // Verificar si existe un rol con ese nombre
    boolean existsByName(String name);
}