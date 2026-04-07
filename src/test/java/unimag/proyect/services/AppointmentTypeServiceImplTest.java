package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.AppointmentTypeMapper;
import unimag.proyect.repositories.AppointmentTypeRepository;
import unimag.proyect.services.impl.AppointmentTypeServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentTypeServiceImplTest {

    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @Mock
    private AppointmentTypeMapper appointmentTypeMapper;

    @InjectMocks
    private AppointmentTypeServiceImpl appointmentTypeService;

    private UUID typeId;
    private AppointmentType appointmentType;
    private AppointmentTypeResponse response;

    @BeforeEach
    void setUp() {
        typeId = UUID.randomUUID();

        appointmentType = new AppointmentType();
        appointmentType.setIdAppointmentType(typeId);
        appointmentType.setName("Consulta General");
        appointmentType.setDuration(30);

        response = new AppointmentTypeResponse(
                typeId,
                "Consulta General",
                30
        );
    }

    @Test
    void create_shouldSaveAndReturnResponse_whenNameIsUnique() {
        CreateAppointmentTypeRequest request = new CreateAppointmentTypeRequest(
                "Consulta General",
                30
        );

        when(appointmentTypeRepository.existsByName("Consulta General")).thenReturn(false);
        when(appointmentTypeMapper.toEntity(request)).thenReturn(appointmentType);
        when(appointmentTypeRepository.save(appointmentType)).thenReturn(appointmentType);
        when(appointmentTypeMapper.toResponse(appointmentType)).thenReturn(response);

        AppointmentTypeResponse result = appointmentTypeService.create(request);

        assertThat(result).isEqualTo(response);
        verify(appointmentTypeRepository).existsByName("Consulta General");
        verify(appointmentTypeMapper).toEntity(request);
        verify(appointmentTypeRepository).save(appointmentType);
        verify(appointmentTypeMapper).toResponse(appointmentType);
    }

    @Test
    void create_shouldThrow_whenNameAlreadyExists() {
        CreateAppointmentTypeRequest request = new CreateAppointmentTypeRequest(
                "Consulta General",
                30
        );

        when(appointmentTypeRepository.existsByName("Consulta General")).thenReturn(true);

        assertThatThrownBy(() -> appointmentTypeService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(appointmentTypeRepository).existsByName("Consulta General");
        verify(appointmentTypeRepository, never()).save(any());
        verifyNoInteractions(appointmentTypeMapper);
    }

    @Test
    void findById_shouldReturnResponse_whenAppointmentTypeExists() {
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(appointmentType));
        when(appointmentTypeMapper.toResponse(appointmentType)).thenReturn(response);

        AppointmentTypeResponse result = appointmentTypeService.findById(typeId);

        assertThat(result).isEqualTo(response);
        verify(appointmentTypeRepository).findById(typeId);
        verify(appointmentTypeMapper).toResponse(appointmentType);
    }

    @Test
    void findById_shouldThrow_whenAppointmentTypeNotFound() {
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentTypeService.findById(typeId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentTypeRepository).findById(typeId);
        verifyNoInteractions(appointmentTypeMapper);
    }

    @Test
    void findAll_shouldReturnMappedList() {
        AppointmentType type2 = new AppointmentType();
        UUID type2Id = UUID.randomUUID();
        type2.setIdAppointmentType(type2Id);
        type2.setName("Psicología");
        type2.setDuration(60);

        AppointmentTypeResponse response2 = new AppointmentTypeResponse(
                type2Id,
                "Psicología",
                60
        );

        when(appointmentTypeRepository.findAll()).thenReturn(List.of(appointmentType, type2));
        when(appointmentTypeMapper.toResponse(appointmentType)).thenReturn(response);
        when(appointmentTypeMapper.toResponse(type2)).thenReturn(response2);

        List<AppointmentTypeResponse> result = appointmentTypeService.findAll();

        assertThat(result).hasSize(2).containsExactly(response, response2);
        verify(appointmentTypeRepository).findAll();
        verify(appointmentTypeMapper, times(2)).toResponse(any(AppointmentType.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoAppointmentTypes() {
        when(appointmentTypeRepository.findAll()).thenReturn(List.of());

        List<AppointmentTypeResponse> result = appointmentTypeService.findAll();

        assertThat(result).isEmpty();
        verify(appointmentTypeRepository).findAll();
        verifyNoInteractions(appointmentTypeMapper);
    }
}