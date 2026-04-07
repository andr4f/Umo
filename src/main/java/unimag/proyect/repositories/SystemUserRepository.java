package unimag.proyect.repositories;

import unimag.proyect.entities.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import unimag.proyect.enums.PersonStatus;

public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {

    // Query Methods — el más importante: usado por Spring Security en el login
    Optional<SystemUser> findByUsername(String username);
    Optional<SystemUser> findByEmail(String email);
    List<SystemUser> findByRole_Name(String roleName);
    List<SystemUser> findByStatus(PersonStatus status);

    // Verificar si ya existe un username (para validar en registro)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Usuario con su rol cargado (evita N+1 al construir el JWT)
    @Query("SELECT u FROM SystemUser u JOIN FETCH u.role WHERE u.username = :username")
    Optional<SystemUser> findByUsernameWithRole(@Param("username") String username);

    // Todos los usuarios de un rol específico
    @Query("SELECT u FROM SystemUser u JOIN FETCH u.role r WHERE r.name = :roleName")
    List<SystemUser> findAllByRoleName(@Param("roleName") String roleName);
}