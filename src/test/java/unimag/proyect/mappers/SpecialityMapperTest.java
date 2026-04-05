package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.entities.Speciality;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialityMapperTest {

    private final SpecialityMapper mapper = Mappers.getMapper(SpecialityMapper.class);

    @Test
    void toEntity_shouldMapName_andIgnoreIdAndDoctors() {
        // Arrange
        CreateSpecialtyRequest request = new CreateSpecialtyRequest("Cardiología");

        // Act
        Speciality entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdSpeciality()).isNull();
        assertThat(entity.getName()).isEqualTo("Cardiología");
        assertThat(entity.getDoctors()).isEmpty();
    }

    @Test
    void toResponse_shouldMapIdAndName() {
        // Arrange
        UUID id = UUID.randomUUID();
        Speciality entity = Speciality.builder()
                .idSpeciality(id)
                .name("Psicología")
                .build();

        // Act
        SpecialityResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Psicología");
    }
}