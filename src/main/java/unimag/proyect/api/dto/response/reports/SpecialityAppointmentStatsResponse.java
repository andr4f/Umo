package unimag.proyect.api.dto.response.reports;

import java.util.UUID;

public record SpecialityAppointmentStatsResponse(
    UUID specialtyId,
    String specialtyName,
    Long cancelledCount,   // citas CANCELLED
    Long noShowCount       // citas NOSHOW
) {}