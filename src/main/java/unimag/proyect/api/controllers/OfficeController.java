package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.services.OfficeService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfficeResponse create(@Valid @RequestBody CreateOfficeRequest request) {
        return officeService.create(request);
    }

    @GetMapping
    public List<OfficeResponse> findAll() {
        return officeService.findAll();
    }

    @PutMapping("/{id}")
    public OfficeResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateOfficeRequest request) {
        return officeService.update(id, request);
    }
}
