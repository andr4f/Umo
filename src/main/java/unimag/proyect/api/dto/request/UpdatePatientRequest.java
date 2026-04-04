package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

public record UpdatePatientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        Gender gender,

        @NotNull(message = "Status is required")
        PersonStatus status
) {}