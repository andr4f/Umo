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

    @Test
    @DisplayName("Find role by name")
    void shouldFindByName() {
        Role role = Role.builder().name("ADMIN").build();
        roleRepository.save(role);

        Optional<Role> result = roleRepository.findByName("ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Return empty if role name not found")
    void shouldReturnEmptyWhenNameNotFound() {
        Optional<Role> result = roleRepository.findByName("NON_EXISTING");
        assertThat(result).isEmpty();
    }
}
