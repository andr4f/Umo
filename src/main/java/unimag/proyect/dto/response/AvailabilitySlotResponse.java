package unimag.proyect.dto.response;

import java.time.LocalDateTime;

public record AvailabilitySlotResponse(
        LocalDateTime startTime,
        LocalDateTime endTime
) {}