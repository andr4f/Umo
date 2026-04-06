package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.request.UpdateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;
import unimag.proyect.entities.Role;
import unimag.proyect.entities.SystemUser;
import unimag.proyect.enums.PersonStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SystemUserMapperTest {

    private final SystemUserMapper mapper = Mappers.getMapper(SystemUserMapper.class);

    @Test
    void toEntity_shouldMapPersonFields_andIgnorePasswordRoleStatus() {
        // Arrange
        CreateSystemUserRequest request = new CreateSystemUserRequest(
                "Admin User",
                "CC",
                "12345",
                "admin@test.com",
                "admin",
                "secretPass",
                UUID.randomUUID()
        );

        // Act
        SystemUser entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getIdPerson()).isNull();
        assertThat(entity.getFullName()).isEqualTo("Admin User");
        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("12345");
        assertThat(entity.getEmail()).isEqualTo("admin@test.com");
        assertThat(entity.getUsername()).isEqualTo("admin");

        assertThat(entity.getPassword()).isNull();  // se encripta en service
        assertThat(entity.getRole()).isNull();      // se asigna en service
        assertThat(entity.getStatus()).isEqualTo(PersonStatus.ACTIVE);    // ACTIVE en service
    }

    @Test
    void toResponse_shouldMapFullNameUsernameAndRoleName() {
        // Arrange
        UUID id = UUID.randomUUID();
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ADMIN");

        SystemUser entity = SystemUser.builder()
                .idPerson(id)
                .fullName("Admin User")
                .documentType("CC")
                .documentNumber("12345")
                .email("admin@test.com")
                .username("admin")
                .role(role)
                .status(PersonStatus.ACTIVE)
                .build();

        // Act
        SystemUserResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.idPerson()).isEqualTo(id);
        assertThat(response.fullName()).isEqualTo("Admin User");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.roleName()).isEqualTo("ADMIN");
    }

    @Test
    void updateEntity_shouldUpdateFullNameAndEmail_andKeepSensitiveFields() {
        // Arrange
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ADMIN");

        SystemUser entity = SystemUser.builder()
                .idPerson(UUID.randomUUID())
                .fullName("Old Name")
                .documentType("CC")
                .documentNumber("12345")
                .email("old@test.com")
                .username("admin")
                .password("hashed")
                .role(role)
                .status(PersonStatus.ACTIVE)
                .build();

        UpdateSystemUserRequest request = new UpdateSystemUserRequest(
                "New Name",
                "new@test.com",
                role.getId()
        );

        // Act
        mapper.updateEntity(request, entity);

        // Assert
        assertThat(entity.getFullName()).isEqualTo("New Name");
        assertThat(entity.getEmail()).isEqualTo("new@test.com");

        // Campos sensibles / de identidad no tocados por el mapper
        assertThat(entity.getUsername()).isEqualTo("admin");
        assertThat(entity.getPassword()).isEqualTo("hashed");
        assertThat(entity.getDocumentType()).isEqualTo("CC");
        assertThat(entity.getDocumentNumber()).isEqualTo("12345");
    }
}