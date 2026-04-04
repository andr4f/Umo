package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.Gender;

import java.util.UUID;

public record CreateDoctorRequest(
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

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Register number is required")
        String registerNum,

        @NotNull(message = "Specialty ID is required")
        UUID specialityId
) {}