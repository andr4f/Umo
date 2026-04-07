package unimag.proyect.api.dto.response.reports;

import unimag.proyect.enums.AppointmentStatus;

public record AppointmentStatusSummaryResponse(
    AppointmentStatus status,   // SCHEDULED, CONFIRMED, COMPLETED...
    Long count,
    Double percentage           // count / total * 100
) {}