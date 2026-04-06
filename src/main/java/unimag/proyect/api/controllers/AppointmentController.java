package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.services.AppointmentService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.create(request);
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable UUID id) {
        return appointmentService.findById(id);
    }

    @GetMapping
    public List<AppointmentResponse> findAll() {
        return appointmentService.findAll();
    }

    @PutMapping("/{id}/confirm")
    public AppointmentResponse confirm(@PathVariable UUID id) {
        return appointmentService.confirm(id);
    }

    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id, @Valid @RequestBody CancelAppointmentRequest request) {
        return appointmentService.cancel(id, request);
    }

    @PutMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable UUID id, 
                                        @RequestParam(required = false, defaultValue = "") String observations) {
        return appointmentService.complete(id, observations);
    }

    @PutMapping("/{id}/no-show")
    public AppointmentResponse markNoShow(@PathVariable UUID id) {
        return appointmentService.markNoShow(id);
    }
}
