package unimag.proyect.dto.response;

import java.util.UUID;

public record NoShowPatientResponse(
        UUID patientId,
        String patientName,
        Long noShowCount
) {}