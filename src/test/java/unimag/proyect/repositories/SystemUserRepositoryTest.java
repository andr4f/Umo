package unimag.proyect.repositories;

import unimag.proyect.entities.Role;
import unimag.proyect.entities.SystemUser;
import unimag.proyect.enums.PersonStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SystemUserRepositoryTest extends AbstractRepositoryIT {

    @Autowired SystemUserRepository systemUserRepository;
    @Autowired RoleRepository roleRepository;

    @BeforeEach
    void clean() {
        systemUserRepository.deleteAll();
        roleRepository.deleteAll();
    }

    // ── helpers ──────────────────────────────────────────────
    private Role savedRole(String name) {
        return roleRepository.save(Role.builder()
                .name(name)
                .build());
    }

    private SystemUser savedSystemUser(String username, String email, Role role) {
        return systemUserRepository.save(SystemUser.builder()
                .fullName("Test User")
                .documentType("CC")
                .documentNumber(username.hashCode() + "")  // ← único por username
                .email(email)
                .phone("3001234567")
                .username(username)
                .password("encoded_password")
                .role(role)
                .build());
    }
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Encuentra un usuario por username")
    void shouldFindByUsername() {
        var role = savedRole("ADMIN");
        savedSystemUser("ana.garcia", "ana@uni.edu", role);

        Optional<SystemUser> result = systemUserRepository.findByUsername("ana.garcia");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("ana.garcia");
    }

    @Test
    @DisplayName("Retorna vacío si el username no existe")
    void shouldReturnEmptyWhenUsernameNotFound() {
        Optional<SystemUser> result = systemUserRepository.findByUsername("noexiste");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Encuentra un usuario por email")
    void shouldFindByEmail() {
        var role = savedRole("RECEPTIONIST");
        savedSystemUser("carlos.p", "carlos@uni.edu", role);

        Optional<SystemUser> result = systemUserRepository.findByEmail("carlos@uni.edu");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("carlos@uni.edu");
    }

    @Test
    @DisplayName("Encuentra usuarios por nombre de rol")
    void shouldFindByRoleName() {
        var adminRole  = savedRole("ADMIN");
        var doctorRole = savedRole("DOCTOR");
        savedSystemUser("user1", "user1@uni.edu", adminRole);   // doc: user1.hashCode()
        savedSystemUser("user2", "user2@uni.edu", adminRole);   // doc: user2.hashCode()
        savedSystemUser("user3", "user3@uni.edu", doctorRole);  // doc: user3.hashCode()

        List<SystemUser> result = systemUserRepository.findByRole_Name("ADMIN");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.getRole().getName().equals("ADMIN"));
    }

    @Test
    @DisplayName("Verifica que existsByUsername retorna true si existe")
    void shouldReturnTrueWhenUsernameExists() {
        var role = savedRole("COORDINATOR");
        savedSystemUser("maria.lopez", "maria@uni.edu", role);

        assertThat(systemUserRepository.existsByUsername("maria.lopez")).isTrue();
    }

    @Test
    @DisplayName("Verifica que existsByUsername retorna false si no existe")
    void shouldReturnFalseWhenUsernameNotExists() {
        assertThat(systemUserRepository.existsByUsername("fantasma")).isFalse();
    }

    @Test
    @DisplayName("Encuentra usuario con rol cargado por username")
    void shouldFindByUsernameWithRole() {
        var role = savedRole("DOCTOR");
        savedSystemUser("dr.house", "house@uni.edu", role);

        Optional<SystemUser> result = systemUserRepository.findByUsernameWithRole("dr.house");

        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isNotNull();
        assertThat(result.get().getRole().getName()).isEqualTo("DOCTOR");
    }

    @Test
    @DisplayName("Encuentra todos los usuarios de un rol específico")
    void shouldFindAllByRoleName() {
        var receptionistRole = savedRole("RECEPTIONIST");
        var adminRole        = savedRole("ADMIN");
        savedSystemUser("recep1", "recep1@uni.edu", receptionistRole);  // doc único
        savedSystemUser("recep2", "recep2@uni.edu", receptionistRole);  // doc único
        savedSystemUser("admin1", "admin1@uni.edu", adminRole);         // doc único

        List<SystemUser> result = systemUserRepository.findAllByRoleName("RECEPTIONIST");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.getRole().getName().equals("RECEPTIONIST"));
    }

    @Test
    @DisplayName("Encuentra usuarios por status")
    void shouldFindByStatus() {
        // Arrange
        var role = savedRole("ADMIN");
        savedSystemUser("activo1", "activo1@uni.edu", role);  // PersonStatus.ACTIVE por defecto
        savedSystemUser("activo2", "activo2@uni.edu", role);

        // Act
        List<SystemUser> result = systemUserRepository.findByStatus(PersonStatus.ACTIVE);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.getStatus() == PersonStatus.ACTIVE);
    }

    @Test
    @DisplayName("Retorna true si el email ya existe")
    void shouldReturnTrueWhenEmailExists() {
        // Arrange
        var role = savedRole("ADMIN");
        savedSystemUser("user.test", "test@uni.edu", role);

        // Act & Assert
        assertThat(systemUserRepository.existsByEmail("test@uni.edu")).isTrue();
    }

    @Test
    @DisplayName("Retorna false si el email no existe")
    void shouldReturnFalseWhenEmailNotExists() {
        // Arrange — BD vacía

        // Act & Assert
        assertThat(systemUserRepository.existsByEmail("noexiste@uni.edu")).isFalse();
    }

}