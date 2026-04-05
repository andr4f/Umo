package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.entities.Speciality;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.SpecialityRepository;
import unimag.proyect.services.SpecialityService;
import unimag.proyect.mappers.SpecialityMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialityServiceImpl implements SpecialityService {

    private final SpecialityRepository specialityRepository;
    private final SpecialityMapper specialityMapper;

    @Override
    public SpecialityResponse create(CreateSpecialtyRequest request) {
        if (specialityRepository.existsByName(request.name())) {
            throw new ConflictException("Speciality name already exists");
        }
        Speciality speciality = specialityMapper.toEntity(request);
        Speciality saved = specialityRepository.save(speciality);
        return specialityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialityResponse findById(UUID id) {
        Speciality speciality = specialityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speciality", id));
        return specialityMapper.toResponse(speciality);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialityResponse> findAll() {
        return specialityRepository.findAll().stream()
                .map(specialityMapper::toResponse)
                .toList();
    }
}