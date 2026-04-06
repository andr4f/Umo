package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.services.SpecialityService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialityService specialityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpecialityResponse create(@Valid @RequestBody CreateSpecialtyRequest request) {
        return specialityService.create(request);
    }

    @GetMapping
    public List<SpecialityResponse> findAll() {
        return specialityService.findAll();
    }
    
    @GetMapping("/{id}")
    public SpecialityResponse findById(@PathVariable UUID id) {
        return specialityService.findById(id);
    }
}
