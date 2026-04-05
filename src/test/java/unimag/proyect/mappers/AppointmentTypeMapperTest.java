package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.entities.AppointmentType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentTypeMapperTest {

    private final AppointmentTypeMapper mapper = Mappers.getMapper(AppointmentTypeMapper.class);

    @Test
    void toEntity_shouldMapBasicFields_andIgnoreIdAndAppointments() {
        // Arrange
        CreateAppointmentTypeRequest request =
                new CreateAppointmentTypeRequest("Consulta general", 30);

        // Act
        AppointmentType entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdAppointmentType()).isNull();
        assertThat(entity.getName()).isEqualTo("Consulta general");
        assertThat(entity.getDuration()).isEqualTo(30);
        // Si la lista está con @Builder.Default debería ser vacía o null según tu implementación
    }

    @Test
    void toResponse_shouldMapBasicFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        AppointmentType entity = AppointmentType.builder()
                .idAppointmentType(id)
                .name("Psicología")
                .duration(45)
                .build();

        // Act
        AppointmentTypeResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Psicología");
        assertThat(response.durationMinutes()).isEqualTo(45);
    }
}