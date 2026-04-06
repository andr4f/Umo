package unimag.proyect.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.services.DoctorScheduleService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorScheduleResponse create(@PathVariable UUID doctorId, 
                                         @Valid @RequestBody CreateDoctorScheduleRequest request) {
        return doctorScheduleService.create(doctorId, request);
    }

    @GetMapping
    public List<DoctorScheduleResponse> findByDoctor(@PathVariable UUID doctorId) {
        return doctorScheduleService.findByDoctor(doctorId);
    }
}
