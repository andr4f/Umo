package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorMapperTest {

    private final DoctorMapper mapper = Mappers.getMapper(DoctorMapper.class);

    @Test
    void toEntity_shouldMapBasicFields_andIgnoreIdAndRelations() {
        // Arrange
        UUID specialityId = UUID.randomUUID();
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Dr. Test",
                "CC",
                "12345",
                "doctor@test.com",
                "3001234567",
                Gender.MALE,
                "REG-001",
                specialityId
        );

        // Act
        Doctor entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdPerson()).isNull();
        assertThat(entity.getFullName()).isEqualTo("Dr. Test");
        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("12345");
        assertThat(entity.getEmail()).isEqualTo("doctor@test.com");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getGender()).isEqualTo(Gender.MALE);
        assertThat(entity.getRegisterNum()).isEqualTo("REG-001");

        assertThat(entity.getSpeciality()).isNull();   // lo resuelve el service
        assertThat(entity.getSchedules()).isEmpty();    // relación lazy
        assertThat(entity.getStatus()).isEqualTo(PersonStatus.ACTIVE);    // se pone ACTIVE en service
    }

    @Test
    void toResponse_shouldMapAllFields_andFlattenSpeciality() {
        // Arrange
        UUID docId = UUID.randomUUID();
        UUID specId = UUID.randomUUID();

        Speciality speciality = Speciality.builder()
                .idSpeciality(specId)
                .name("Cardiología")
                .build();

        Doctor entity = Doctor.builder()
                .idPerson(docId)
                .fullName("Dr. House")
                .documentType("CC")
                .documentNumber("98765")
                .email("house@test.com")
                .phone("3000000000")
                .gender(Gender.MALE)
                .registerNum("REG-999")
                .speciality(speciality)
                .status(PersonStatus.ACTIVE)
                .build();

        // Act
        DoctorResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(docId);
        assertThat(response.fullName()).isEqualTo("Dr. House");
        assertThat(response.documentType()).isEqualTo("CC");
        assertThat(response.documentNumber()).isEqualTo("98765");
        assertThat(response.email()).isEqualTo("house@test.com");
        assertThat(response.phone()).isEqualTo("3000000000");
        assertThat(response.gender()).isEqualTo(Gender.MALE);
        assertThat(response.registerNum()).isEqualTo("REG-999");
        assertThat(response.specialityId()).isEqualTo(specId);
        assertThat(response.specialityName()).isEqualTo("Cardiología");
        assertThat(response.status()).isEqualTo(PersonStatus.ACTIVE);
    }

    @Test
    void updateEntity_shouldUpdateMutableFields_andKeepIgnoredFields() {
        // Arrange
        Speciality originalSpec = Speciality.builder()
                .idSpeciality(UUID.randomUUID())
                .name("Original")
                .build();

        Doctor entity = Doctor.builder()
                .idPerson(UUID.randomUUID())
                .fullName("Dr. Old")
                .documentType("CC")
                .documentNumber("123")
                .email("old@test.com")
                .phone("3000000000")
                .gender(Gender.FEMALE)
                .registerNum("REG-OLD")
                .speciality(originalSpec)
                .status(PersonStatus.ACTIVE)
                .build();

        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "Dr. New",
                "new@test.com",
                "3111111111",
                Gender.MALE,
                "REG-NEW",
                originalSpec.getIdSpeciality()
        );

        // Act
        mapper.updateEntity(entity, request);

        // Assert
        assertThat(entity.getFullName()).isEqualTo("Dr. New");
        assertThat(entity.getEmail()).isEqualTo("new@test.com");
        assertThat(entity.getPhone()).isEqualTo("3111111111");
        assertThat(entity.getGender()).isEqualTo(Gender.MALE);
        assertThat(entity.getRegisterNum()).isEqualTo("REG-NEW");

        // Ignorados
        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("123");
        assertThat(entity.getIdPerson()).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(PersonStatus.ACTIVE);
        // speciality se reasigna en service, aquí mantenemos la misma instancia
    }
}