package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    PatientResponse create(CreatePatientRequest request);

    PatientResponse findById(UUID id);

    Page<PatientResponse> findAll(Pageable pageable);

    PatientResponse update(UUID id, UpdatePatientRequest request);
}