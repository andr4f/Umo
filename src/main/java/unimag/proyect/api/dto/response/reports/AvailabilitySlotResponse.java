package unimag.proyect.api.dto.response.reports;

import java.time.LocalDateTime;

// ¿Qué slots libres tiene el doctor en esa fecha?
public record AvailabilitySlotResponse(
        LocalDateTime startTime,
        LocalDateTime endTime
) {}