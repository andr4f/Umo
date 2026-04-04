package unimag.proyect.api.dto.response.reports;

import java.time.LocalDate;
import java.util.UUID;

public record OfficeOccupancyResponse(
        UUID officeId,
        String officeCode,
        LocalDate date,
        Integer totalSlots,
        Integer occupiedSlots,
        Double occupancyPercentage
) {}