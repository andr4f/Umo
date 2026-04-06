package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ConflictException;
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
                .ifPresent(d -> { throw new ConflictException("Email already in use"); });

        doctorRepository.findByRegisterNum(request.registerNum())
                .ifPresent(d -> { throw new ConflictException("Register number already in use"); });

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
    public List<DoctorResponse> findAll() {
        return doctorRepository.findByStatus(PersonStatus.ACTIVE).stream()
                .map(doctorMapper::toResponse)
                .toList();
    }

    @Override
    public DoctorResponse update(UUID id, UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        Speciality speciality = specialityRepository.findById(request.specialityId())
                .orElseThrow(() -> new ResourceNotFoundException("Speciality", request.specialityId()));

        doctorMapper.updateEntity(doctor,request);
        doctor.setSpeciality(speciality);

        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toResponse(saved);
    }
}