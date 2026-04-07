package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    private final PatientMapper mapper = Mappers.getMapper(PatientMapper.class);

    @Test
    void toEntity_shouldMapBasicFields_andIgnoreIdAndAppointments() {
        // Arrange
        CreatePatientRequest request = new CreatePatientRequest(
                "Paciente Test",
                "CC",
                "1000000",
                "paciente@test.com",
                "3009999999",
                Gender.FEMALE
        );

        // Act
        Patient entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdPerson()).isNull();
        assertThat(entity.getFullName()).isEqualTo("Paciente Test");
        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("1000000");
        assertThat(entity.getEmail()).isEqualTo("paciente@test.com");
        assertThat(entity.getPhone()).isEqualTo("3009999999");
        assertThat(entity.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(entity.getAppointments()).isEmpty();
        assertThat(entity.getStatus()).isEqualTo(PersonStatus.ACTIVE); // se pone ACTIVE en service
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        Patient entity = Patient.builder()
                .idPerson(id)
                .fullName("Paciente Test")
                .documentType("CC")
                .documentNumber("1000000")
                .email("paciente@test.com")
                .phone("3009999999")
                .gender(Gender.FEMALE)
                .status(PersonStatus.ACTIVE)
                .build();

        // Act
        PatientResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.fullName()).isEqualTo("Paciente Test");
        assertThat(response.documentType()).isEqualTo("CC");
        assertThat(response.documentNumber()).isEqualTo("1000000");
        assertThat(response.email()).isEqualTo("paciente@test.com");
        assertThat(response.phone()).isEqualTo("3009999999");
        assertThat(response.gender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void updateEntity_shouldUpdateMutableFields_andKeepIgnoredFields() {
        // Arrange
        Patient entity = Patient.builder()
                .idPerson(UUID.randomUUID())
                .fullName("Old Name")
                .documentType("CC")
                .documentNumber("1000000")
                .email("old@test.com")
                .phone("3000000000")
                .gender(Gender.MALE)
                .status(PersonStatus.ACTIVE)
                .build();

        UpdatePatientRequest request = new UpdatePatientRequest(
                "New Name",
                "new@test.com",
                "3111111111",
                Gender.FEMALE,
                PersonStatus.ACTIVE
        );

        // Act
        mapper.updateEntity(entity, request);

        // Assert
        assertThat(entity.getFullName()).isEqualTo("New Name");
        assertThat(entity.getEmail()).isEqualTo("new@test.com");
        assertThat(entity.getPhone()).isEqualTo("3111111111");
        assertThat(entity.getGender()).isEqualTo(Gender.FEMALE);

        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("1000000");
        assertThat(entity.getStatus()).isEqualTo(PersonStatus.ACTIVE);
        assertThat(entity.getIdPerson()).isNotNull();
    }
}