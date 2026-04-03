package unimag.proyect.dto.response;

import java.util.UUID;

public record AppointmentTypeResponse(
        UUID id,
        String name,
        Integer durationMinutes
) {}