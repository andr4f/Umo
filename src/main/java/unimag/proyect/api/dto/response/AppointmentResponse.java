package unimag.proyect.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import unimag.proyect.enums.AppointmentStatus;

public record AppointmentResponse(
    UUID id,
    PatientResponse patient,
    DoctorResponse doctor,
    OfficeResponse office,
    AppointmentTypeResponse appointmentType,
    LocalDateTime startTime,
    LocalDateTime endTime,
    AppointmentStatus status,
    String cancelReason,
    String observations
) {}