package unimag.proyect.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import unimag.proyect.enums.Gender;

public record CreatePatientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document number is required")
        String documentNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        Gender gender
) {}