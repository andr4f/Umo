package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequest(
        @NotBlank(message = "Cancel reason is required")
        String cancelReason
) {}