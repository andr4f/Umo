package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotNull(message = "Office ID is required")
        UUID officeId,

        @NotNull(message = "Appointment type ID is required")
        UUID appointmentTypeId,

        @NotNull(message = "Start time is required")
        @FutureOrPresent(message = "Appointment cannot be in the past")
        LocalDateTime startTime
) {}