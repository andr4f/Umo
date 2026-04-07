package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.entities.Office;
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.OfficeRepository;
import unimag.proyect.services.OfficeService;
import unimag.proyect.mappers.OfficeMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final OfficeMapper officeMapper;
    private final AppointmentRepository appointmentRepository;

    @Override
    public OfficeResponse create(CreateOfficeRequest request) {
        if (officeRepository.existsByCode(request.code())) {
            throw new ConflictException("Office code already exists");
        }
        Office office = officeMapper.toEntity(request);
        office.setStatus(OfficeStatus.ACTIVE);

        Office saved = officeRepository.save(office);
        return officeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeResponse> findAll() {
        return officeRepository.findAll().stream()
                .map(officeMapper::toResponse)
                .toList();
    }

    @Override
    public OfficeResponse update(UUID id, UpdateOfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));

        // Validar antes de desactivar
        if (request.status() == OfficeStatus.INACTIVE
                && office.getStatus() == OfficeStatus.ACTIVE) {
            if (appointmentRepository.existsActiveAppointmentsByOffice(id)) {
                throw new BusinessException(
                    "Cannot deactivate office with active appointments");
            }
        }

        officeMapper.updateEntity(office, request);
        Office saved = officeRepository.save(office);
        return officeMapper.toResponse(saved);
    }
}