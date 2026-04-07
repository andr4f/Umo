package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAppointmentTypeRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than 0")
        Integer durationMinutes
) {}