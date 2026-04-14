package unimag.proyect.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.services.AppointmentService;


@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Validated

public class AppointmentController {

    private final AppointmentService service;
    
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request,
            UriComponentsBuilder uriBuilder) {
        
        var AppointmentCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/appointments/{id}").buildAndExpand(AppointmentCreated.id()).toUri();
        return ResponseEntity.created(location).body(AppointmentCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable UUID id) {
        var appointment = service.findById(id);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("idAppointment").ascending()));
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable UUID id) {
        var appointment = service.confirm(id);
        return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable UUID id, @Valid @RequestBody CancelAppointmentRequest request) {
        var appointment = service.cancel(id, request);
        return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable UUID id, @RequestParam(required = false) String observations) {
        var appointment = service.complete(id, observations);
        return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markNoShow(@PathVariable UUID id) {
        var appointment = service.markNoShow(id);
        return ResponseEntity.ok(appointment);
    }

}
