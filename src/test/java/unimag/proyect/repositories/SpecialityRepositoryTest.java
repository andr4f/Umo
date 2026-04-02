package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Speciality;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialityRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private SpecialityRepository specialityRepository;

    @BeforeEach
    void clean() {
        specialityRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find Speciality by name")
    void shouldFindSpecialityByName() {
        Speciality spec = Speciality.builder().name("Cardiology").build();
        specialityRepository.save(spec);

        Optional<Speciality> found = specialityRepository.findAll().stream()
                .filter(s -> s.getName().equals("Cardiology"))
                .findFirst();

        assertThat(found).isPresent();
    }
}
