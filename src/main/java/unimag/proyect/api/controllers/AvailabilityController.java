package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;
import unimag.proyect.services.AvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/doctors/{doctorId}")
    public List<AvailabilitySlotResponse> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID appointmentTypeId) {
        return availabilityService.getAvailableSlots(doctorId, date, appointmentTypeId);
    }
}
