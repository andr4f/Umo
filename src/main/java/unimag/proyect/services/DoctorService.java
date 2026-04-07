package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;

import java.util.List;
import java.util.UUID;

public interface DoctorService {

    DoctorResponse create(CreateDoctorRequest request);

    DoctorResponse findById(UUID id);

    List<DoctorResponse> findAll();

    DoctorResponse update(UUID id, UpdateDoctorRequest request);
}