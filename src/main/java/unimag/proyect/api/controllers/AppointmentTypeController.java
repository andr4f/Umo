package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.services.AppointmentTypeService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/appointment-types")
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeService appointmentTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentTypeResponse create(@Valid @RequestBody CreateAppointmentTypeRequest request) {
        return appointmentTypeService.create(request);
    }

    @GetMapping
    public List<AppointmentTypeResponse> findAll() {
        return appointmentTypeService.findAll();
    }
}
