package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.DuplicateResourceException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.SpecialityRepository;
import unimag.proyect.services.DoctorService;
import unimag.proyect.mappers.DoctorMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecialityRepository specialityRepository;
    private final DoctorMapper doctorMapper;

    @Override
    public DoctorResponse create(CreateDoctorRequest request) {
        doctorRepository.findByEmail(request.email())
                .ifPresent(d -> { throw new DuplicateResourceException("email", request.email()); });

        doctorRepository.findByRegisterNum(request.registerNum())
                .ifPresent(d -> { throw new DuplicateResourceException("registerNum", request.registerNum()); });

        Speciality speciality = specialityRepository.findById(request.specialityId())
                .orElseThrow(() -> new ResourceNotFoundException("Speciality", request.specialityId()));

        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setSpeciality(speciality);
        doctor.setStatus(PersonStatus.ACTIVE);

        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse findById(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> findAll(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(doctorMapper::toResponse);
    }

    @Override
    public DoctorResponse update(UUID id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        if (!doctor.getEmail().equals(request.email())
                && doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email", request.email());
        }

        if (!doctor.getRegisterNum().equals(request.registerNum())
                && doctorRepository.existsByRegisterNum(request.registerNum())) {
            throw new DuplicateResourceException("registerNum", request.registerNum());
        }

        Speciality speciality = specialityRepository.findById(request.specialityId())
                .orElseThrow(() -> new ResourceNotFoundException("Speciality", request.specialityId()));

        doctorMapper.updateEntity(doctor,request);
        doctor.setSpeciality(speciality);

        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toResponse(saved);
    }
}