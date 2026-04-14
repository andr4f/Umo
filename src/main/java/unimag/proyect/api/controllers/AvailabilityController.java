package unimag.proyect.api.controllers;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;
import unimag.proyect.services.AvailabilityService;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Validated
public class AvailabilityController {

    private final AvailabilityService service;

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam LocalDate date,
            @RequestParam UUID appointmentTypeId) {

        var slots = service.getAvailableSlots(doctorId, date, appointmentTypeId);
        return ResponseEntity.ok(slots);
    }
}