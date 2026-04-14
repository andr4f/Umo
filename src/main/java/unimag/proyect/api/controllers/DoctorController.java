package unimag.proyect.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.services.DoctorService;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Validated

public class DoctorController {

    private final DoctorService service;

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request,
         UriComponentsBuilder uriBuilder) {
        var doctorCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/doctors/{id}").buildAndExpand(doctorCreated.id()).toUri();
        return ResponseEntity.created(location).body(doctorCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable UUID id) {
        var doctor = service.findById(id);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        var result = service.findAll(PageRequest.of(page, size, Sort.by("idPerson").ascending()));
        return ResponseEntity.ok(result);
    }
    
}
