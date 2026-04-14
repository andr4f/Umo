package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    DoctorResponse create(CreateDoctorRequest request);

    DoctorResponse findById(UUID id);

    Page<DoctorResponse> findAll(Pageable pageable);

    DoctorResponse update(UUID id, UpdateDoctorRequest request);
}