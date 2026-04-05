package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.AppointmentTypeRepository;
import unimag.proyect.services.AppointmentTypeService;
import unimag.proyect.mappers.AppointmentTypeMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentTypeServiceImpl implements AppointmentTypeService {

    private final AppointmentTypeRepository appointmentTypeRepository;
    private final AppointmentTypeMapper appointmentTypeMapper;

    @Override
    public AppointmentTypeResponse create(CreateAppointmentTypeRequest request) {
        if (appointmentTypeRepository.existsByName(request.name())) {
            throw new ConflictException("Appointment type name already exists");
        }

        AppointmentType type = appointmentTypeMapper.toEntity(request);
        AppointmentType saved = appointmentTypeRepository.save(type);
        return appointmentTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentTypeResponse findById(UUID id) {
        AppointmentType type = appointmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType", id));
        return appointmentTypeMapper.toResponse(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentTypeResponse> findAll() {
        return appointmentTypeRepository.findAll().stream()
                .map(appointmentTypeMapper::toResponse)
                .toList();
    }
}