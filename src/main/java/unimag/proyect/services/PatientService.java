package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;

import java.util.List;
import java.util.UUID;

public interface PatientService {

    PatientResponse create(CreatePatientRequest request);

    PatientResponse findById(UUID id);

    List<PatientResponse> findAll();

    PatientResponse update(UUID id, UpdatePatientRequest request);
}