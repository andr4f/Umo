package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateSystemUserRequest(

    @NotBlank
    @Size(max = 150)
    String fullName,          // ← en lugar de firstName + lastName

    @NotBlank
    @Size(max = 100)
    String email,             // ← agregar, es nullable = false en Person

    @NotNull
    UUID roleId               // ← para cambiar rol desde el service
    // username → ignorado en mapper, endpoint aparte
) {}