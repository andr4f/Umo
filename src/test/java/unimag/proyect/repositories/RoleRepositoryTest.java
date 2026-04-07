package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void clean() {
        roleRepository.deleteAll();
    }

    // ── helper ───────────────────────────────────────────────
    private Role savedRole(String name) {
        return roleRepository.save(Role.builder().name(name).build());
    }
    // ─────────────────────────────────────────────────────────

    // ── findByName ───────────────────────────────────────────

    @Test
    @DisplayName("Encuentra un rol por nombre exacto")
    void shouldFindByName() {
        // Arrange
        savedRole("ADMIN");

        // Act
        Optional<Role> result = roleRepository.findByName("ADMIN");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Retorna vacío si el nombre del rol no existe")
    void shouldReturnEmptyWhenNameNotFound() {
        // Arrange — BD vacía

        // Act
        Optional<Role> result = roleRepository.findByName("NON_EXISTING");

        // Assert
        assertThat(result).isEmpty();
    }

    // ── existsByName ─────────────────────────────────────────

    @Test
    @DisplayName("Retorna true si el rol existe por nombre")
    void shouldReturnTrueWhenRoleExists() {
        // Arrange
        savedRole("DOCTOR");

        // Act
        boolean exists = roleRepository.existsByName("DOCTOR");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Retorna false si el rol no existe")
    void shouldReturnFalseWhenRoleNotExists() {
        // Arrange — BD vacía

        // Act
        boolean exists = roleRepository.existsByName("GHOST_ROLE");

        // Assert
        assertThat(exists).isFalse();
    }
}