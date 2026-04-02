package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.AppointmentType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentTypeRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @BeforeEach
    void clean() {
        appointmentTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find AppointmentType by name")
    void shouldFindAppointmentTypeByName() {
        AppointmentType type = AppointmentType.builder()
                .name("General Checkup")
                .duration(30)
                .build();
        appointmentTypeRepository.save(type);

        Optional<AppointmentType> found = appointmentTypeRepository.findAll().stream()
                .filter(t -> t.getName().equals("General Checkup"))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getDuration()).isEqualTo(30);
    }
}
