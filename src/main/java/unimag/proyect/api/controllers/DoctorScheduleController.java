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
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.services.DoctorScheduleService;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
@Validated

public class DoctorScheduleController {

    private final DoctorScheduleService service;
    
    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> create(@PathVariable UUID doctorId,
         @Valid @RequestBody CreateDoctorScheduleRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        var doctorScheduleCreated = service.create(doctorId, request);
        var location = uriBuilder.path("/api/v1/doctors/{doctorId}/schedules/{id}")
                .buildAndExpand(doctorId, doctorScheduleCreated.id()).toUri();
        return ResponseEntity.created(location).body(doctorScheduleCreated);
    }

    @GetMapping
    public ResponseEntity<List<DoctorScheduleResponse>> listByDoctor(@PathVariable UUID doctorId) {
        var doctorSchedules = service.findByDoctor(doctorId);
        return ResponseEntity.ok(doctorSchedules);
    }

}
