package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSystemUserRequest(

    @NotBlank @Size(max = 150)
    String fullName,          // ← en lugar de firstName + lastName

    @NotBlank @Size(max = 20)
    String documentType,      // ← agregar

    @NotBlank @Size(max = 50)
    String documentNumber,    // ← agregar

    @NotBlank @Email
    String email,             // ← agregar

    @NotBlank @Size(max = 50)
    String username,

    @NotBlank @Size(min = 8)
    String password,

    @NotNull
    UUID roleId
) {}