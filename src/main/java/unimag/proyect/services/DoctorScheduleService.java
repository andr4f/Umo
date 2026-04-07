package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;

import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {

    // doctorId es path variable en el controller: POST /api/doctors/{doctorId}/schedules
    DoctorScheduleResponse create(UUID doctorId, CreateDoctorScheduleRequest request);

    List<DoctorScheduleResponse> findByDoctor(UUID doctorId);
}