package unimag.proyect.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import unimag.proyect.entities.Office;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private OfficeRepository officeRepository;

    @BeforeEach
    void clean() {
        officeRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find Office by code")
    void shouldFindOfficeByCode() {
        Office office = Office.builder()
                .code("OFC-01")
                .name("Main Office")
                .location("Building A")
                .build();
        officeRepository.save(office);

        Optional<Office> found = officeRepository.findAll().stream()
                .filter(o -> o.getCode().equals("OFC-01"))
                .findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Main Office");
    }
}
