package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.DuplicateResourceException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.AppointmentRepository;
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
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;

    @Override
    public PatientResponse create(CreatePatientRequest request) {
        patientRepository.findByEmail(request.email())
                .ifPresent(p -> { throw new DuplicateResourceException("email", request.email()); });

        patientRepository.findByDocumentNumber(request.documentNumber())
                .ifPresent(p -> { throw new DuplicateResourceException("documentNumber", request.documentNumber()); });

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
    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toResponse);
    }

    @Override
    public PatientResponse update(UUID id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        if (request.email() != null
                && !patient.getEmail().equals(request.email())
                && patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email", request.email());
        }

        if (patient.getStatus() == PersonStatus.ACTIVE
                && request.status() == PersonStatus.INACTIVE
                && appointmentRepository.existsActiveAppointmentsByPatient(id)) {
            throw new BusinessException("Cannot deactivate patient with active appointments");
        }

        patientMapper.updateEntity(patient, request);
        if (request.status() != null) {
            patient.setStatus(request.status());
        }
        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }
}