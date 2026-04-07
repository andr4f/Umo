package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.entities.Office;
import unimag.proyect.enums.OfficeStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeMapperTest {

    private final OfficeMapper mapper = Mappers.getMapper(OfficeMapper.class);

    @Test
    void toEntity_shouldMapFields_andIgnoreIdAndStatus() {
        // Arrange
        CreateOfficeRequest request = new CreateOfficeRequest(
                "OFC-01",
                "Consultorio 1",
                "Bloque A"
        );

        // Act
        Office entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdOffice()).isNull();
        assertThat(entity.getCode()).isEqualTo("OFC-01");
        assertThat(entity.getName()).isEqualTo("Consultorio 1");
        assertThat(entity.getLocation()).isEqualTo("Bloque A");
        assertThat(entity.getStatus()).isEqualTo(OfficeStatus.ACTIVE); // se pone ACTIVE en service
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        Office entity = Office.builder()
                .idOffice(id)
                .code("OFC-01")
                .name("Consultorio 1")
                .location("Bloque A")
                .status(OfficeStatus.ACTIVE)
                .build();

        // Act
        OfficeResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.code()).isEqualTo("OFC-01");
        assertThat(response.name()).isEqualTo("Consultorio 1");
        assertThat(response.location()).isEqualTo("Bloque A");
        assertThat(response.status()).isEqualTo(OfficeStatus.ACTIVE);
    }

    @Test
    void updateEntity_shouldUpdateMutableFields_andKeepIgnoredFields() {
        // Arrange
        Office entity = Office.builder()
                .idOffice(UUID.randomUUID())
                .code("OFC-01")
                .name("Old Name")
                .location("Old Location")
                .status(OfficeStatus.ACTIVE)
                .build();

        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "New Name",
                "New Location",
                OfficeStatus.UNDER_MAINTENANCE
        );

        // Act
        mapper.updateEntity(entity, request);

        // Assert
        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getLocation()).isEqualTo("New Location");
        assertThat(entity.getStatus()).isEqualTo(OfficeStatus.UNDER_MAINTENANCE);

        assertThat(entity.getCode()).isEqualTo("OFC-01");
        assertThat(entity.getIdOffice()).isNotNull();
    }
}