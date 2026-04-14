package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.services.AppointmentTypeService;

@RestController
@RequestMapping("/api/v1/appointment-types")
@RequiredArgsConstructor
@Validated

public class AppointmentTypeController {

    private final AppointmentTypeService service;

    @PostMapping
    public ResponseEntity<AppointmentTypeResponse> create(@Valid @RequestBody CreateAppointmentTypeRequest request,
            UriComponentsBuilder uriBuilder) {
        var appointmentTypeCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/appointment-types/{id}").buildAndExpand(appointmentTypeCreated.id()).toUri();
        return ResponseEntity.created(location).body(appointmentTypeCreated);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentTypeResponse> getById(@PathVariable UUID id) {
        var appointmentType = service.findById(id);
        return ResponseEntity.ok(appointmentType);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentTypeResponse>> list() {
        var result = service.findAll();
        return ResponseEntity.ok(result);
    }

}
