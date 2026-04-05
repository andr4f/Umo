package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.PatientRepository;
import unimag.proyect.services.PatientService;
import unimag.proyect.mappers.PatientMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    public PatientResponse create(CreatePatientRequest request) {
        patientRepository.findByEmail(request.email())
                .ifPresent(p -> { throw new ConflictException("Email already in use"); });

        patientRepository.findByDocumentNumber(request.documentNumber())
                .ifPresent(p -> { throw new ConflictException("Document number already in use"); });

        Patient patient = patientMapper.toEntity(request);
        patient.setStatus(PersonStatus.ACTIVE);

        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse findById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    @Override
    public PatientResponse update(UUID id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        patientMapper.updateEntity(patient, request);
        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }
}