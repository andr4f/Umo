package unimag.proyect.api.dto.response.reports;

import java.time.LocalDate;
import java.util.List;

public record DailyAgendaResponse(
    LocalDate date,
    String doctorName,
    Integer totalSlots,
    Integer occupiedSlots,
    Integer freeSlots,
    List<AvailabilitySlotResponse> availableSlots
) {}
