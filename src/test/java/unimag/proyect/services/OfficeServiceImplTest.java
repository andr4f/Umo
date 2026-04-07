package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.entities.Office;
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.OfficeMapper;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.OfficeRepository;
import unimag.proyect.services.impl.OfficeServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficeServiceImplTest {

    @Mock private OfficeRepository officeRepository;
    @Mock private OfficeMapper officeMapper;
    @Mock private AppointmentRepository appointmentRepository;

    @InjectMocks
    private OfficeServiceImpl officeService;

    private UUID officeId;
    private Office office;
    private OfficeResponse response;

    @BeforeEach
    void setUp() {
        officeId = UUID.randomUUID();

        office = new Office();
        office.setIdOffice(officeId);
        office.setCode("C-101");
        office.setName("Consultorio 101");
        office.setLocation("Bloque A");
        office.setStatus(OfficeStatus.ACTIVE);

        response = new OfficeResponse(
                officeId,
                "C-101",
                "Consultorio 101",
                "Bloque A",
                OfficeStatus.ACTIVE
        );
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturnResponse_whenCodeIsUnique() {
        CreateOfficeRequest request = new CreateOfficeRequest(
                "C-101",
                "Consultorio 101",
                "Bloque A"
        );

        when(officeRepository.existsByCode("C-101")).thenReturn(false);
        when(officeMapper.toEntity(request)).thenReturn(office);
        when(officeRepository.save(office)).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(response);

        OfficeResponse result = officeService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(office.getStatus()).isEqualTo(OfficeStatus.ACTIVE);
        verify(officeRepository).existsByCode("C-101");
        verify(officeRepository).save(office);
        verify(officeMapper).toEntity(request);
        verify(officeMapper).toResponse(office);
    }

    @Test
    void create_shouldThrow_whenCodeAlreadyExists() {
        CreateOfficeRequest request = new CreateOfficeRequest(
                "C-101",
                "Consultorio 101",
                "Bloque A"
        );

        when(officeRepository.existsByCode("C-101")).thenReturn(true);

        assertThatThrownBy(() -> officeService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(officeRepository, never()).save(any());
        verifyNoInteractions(officeMapper);
        verifyNoInteractions(appointmentRepository);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnMappedList() {
        Office office2 = new Office();
        office2.setIdOffice(UUID.randomUUID());
        office2.setCode("C-102");
        office2.setName("Consultorio 102");
        office2.setLocation("Bloque B");
        office2.setStatus(OfficeStatus.INACTIVE);

        OfficeResponse response2 = new OfficeResponse(
                office2.getIdOffice(),
                "C-102",
                "Consultorio 102",
                "Bloque B",
                OfficeStatus.INACTIVE
        );

        when(officeRepository.findAll()).thenReturn(List.of(office, office2));
        when(officeMapper.toResponse(office)).thenReturn(response);
        when(officeMapper.toResponse(office2)).thenReturn(response2);

        List<OfficeResponse> result = officeService.findAll();

        assertThat(result).hasSize(2).containsExactly(response, response2);
        verify(officeRepository).findAll();
        verify(officeMapper, times(2)).toResponse(any(Office.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoOffices() {
        when(officeRepository.findAll()).thenReturn(List.of());

        List<OfficeResponse> result = officeService.findAll();

        assertThat(result).isEmpty();
        verify(officeRepository).findAll();
        verifyNoInteractions(officeMapper);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldMutateAndSave_whenOfficeExistsAndNoDeactivationConflict() {
        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "Consultorio Renovado",
                "Bloque C",
                OfficeStatus.ACTIVE
        );

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(officeRepository.save(office)).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(response);

        OfficeResponse result = officeService.update(officeId, request);

        assertThat(result).isEqualTo(response);
        verify(officeMapper).updateEntity(office, request);
        verify(officeRepository).save(office);
        verify(appointmentRepository, never()).existsActiveAppointmentsByOffice(any());
    }

    @Test
    void update_shouldThrow_whenOfficeNotFound() {
        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "Consultorio Renovado",
                "Bloque C",
                OfficeStatus.ACTIVE
        );

        when(officeRepository.findById(officeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> officeService.update(officeId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(officeMapper, never()).updateEntity(any(), any());
        verify(officeRepository, never()).save(any());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void update_shouldThrow_whenTryingToDeactivateOfficeWithActiveAppointments() {
        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "Consultorio 101",
                "Bloque A",
                OfficeStatus.INACTIVE
        );

        office.setStatus(OfficeStatus.ACTIVE);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentRepository.existsActiveAppointmentsByOffice(officeId)).thenReturn(true);

        assertThatThrownBy(() -> officeService.update(officeId, request))
                .isInstanceOf(BusinessException.class);

        verify(appointmentRepository).existsActiveAppointmentsByOffice(officeId);
        verify(officeMapper, never()).updateEntity(any(), any());
        verify(officeRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowDeactivation_whenOfficeHasNoActiveAppointments() {
        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "Consultorio 101",
                "Bloque A",
                OfficeStatus.INACTIVE
        );

        office.setStatus(OfficeStatus.ACTIVE);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentRepository.existsActiveAppointmentsByOffice(officeId)).thenReturn(false);
        when(officeRepository.save(office)).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(response);

        OfficeResponse result = officeService.update(officeId, request);

        assertThat(result).isEqualTo(response);
        verify(appointmentRepository).existsActiveAppointmentsByOffice(officeId);
        verify(officeMapper).updateEntity(office, request);
        verify(officeRepository).save(office);
    }

    @Test
    void update_shouldNotCheckAppointments_whenOfficeAlreadyInactive() {
        UpdateOfficeRequest request = new UpdateOfficeRequest(
                "Consultorio 101",
                "Bloque A",
                OfficeStatus.INACTIVE
        );

        office.setStatus(OfficeStatus.INACTIVE);

        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(officeRepository.save(office)).thenReturn(office);
        when(officeMapper.toResponse(office)).thenReturn(response);

        OfficeResponse result = officeService.update(officeId, request);

        assertThat(result).isEqualTo(response);
        verify(appointmentRepository, never()).existsActiveAppointmentsByOffice(any());
        verify(officeMapper).updateEntity(office, request);
        verify(officeRepository).save(office);
    }
}