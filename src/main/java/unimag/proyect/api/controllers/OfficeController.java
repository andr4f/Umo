package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.services.OfficeService;

@RestController
@RequestMapping("/api/v1/offices")
@RequiredArgsConstructor
@Validated

public class OfficeController {

    private final OfficeService service;

    @PostMapping
    public ResponseEntity<OfficeResponse> create(@Valid @RequestBody CreateOfficeRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var officeCreated = service.create(request);
        var location = uriBuilder.path("/api/v1/offices/{id}").buildAndExpand(officeCreated.id()).toUri();
        return ResponseEntity.created(location).body(officeCreated);
    }

    @GetMapping
    public ResponseEntity<List<OfficeResponse>> list() {
        var result = service.findAll();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OfficeResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateOfficeRequest request) {
        var officeUpdated = service.update(id, request);
        return ResponseEntity.ok(officeUpdated);
    }
    
}
