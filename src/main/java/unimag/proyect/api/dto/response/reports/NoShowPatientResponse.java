package unimag.proyect.api.dto.response.reports;

import java.util.UUID;

public record NoShowPatientResponse(
        UUID patientId,
        String patientName,
        Long noShowCount
) {}