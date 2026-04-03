package unimag.proyect.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

import java.util.UUID;

public record UpdateDoctorRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Register number is required")
        String registerNum,

        @NotNull(message = "Specialty ID is required")
        UUID specialityId,

        @NotNull(message = "Status is required")
        PersonStatus status
) {}