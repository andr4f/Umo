package unimag.proyect.api.dto.response;

import java.util.UUID;

public record AppointmentTypeResponse(
        UUID id,
        String name,
        Integer durationMinutes
) {}