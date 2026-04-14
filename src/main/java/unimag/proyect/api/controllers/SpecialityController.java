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
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.services.SpecialityService;

@RestController
@RequestMapping("/api/v1/specialities")
@RequiredArgsConstructor
@Validated

public class SpecialityController {
    
    private final SpecialityService service;


    @PostMapping
    public ResponseEntity<SpecialityResponse> create(@Valid @RequestBody CreateSpecialtyRequest request,
         UriComponentsBuilder uriBuilder) {
        var specialityCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/specialities/{id}").buildAndExpand(specialityCreated.id()).toUri();
        return ResponseEntity.created(location).body(specialityCreated);

    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialityResponse> getById(@PathVariable UUID id) {
        var speciality = service.findById(id);
        return ResponseEntity.ok(speciality);
    }

    @GetMapping
    public ResponseEntity<List<SpecialityResponse>> list() {
        var result = service.findAll();
        return ResponseEntity.ok(result);
    }
    
}
