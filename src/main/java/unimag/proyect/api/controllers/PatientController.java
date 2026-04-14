package unimag.proyect.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.*;
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
import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.services.PatientService;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Validated

public class PatientController {
    
    private final PatientService service;

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request,
         UriComponentsBuilder uriBuilder) {
        var patientCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/patients/{id}").buildAndExpand(patientCreated.id()).toUri();
        return ResponseEntity.created(location).body(patientCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable UUID id) {
        var patient = service.findById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("idPerson").ascending()));
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdatePatientRequest request) {
        var updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }
}
