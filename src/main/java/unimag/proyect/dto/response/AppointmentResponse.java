package unimag.proyect.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import unimag.proyect.enums.AppointmentStatus;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        UUID officeId,
        UUID appointmentTypeId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        String cancelReason,
        String observations
) {}