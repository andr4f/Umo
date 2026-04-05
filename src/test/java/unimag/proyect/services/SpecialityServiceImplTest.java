package unimag.proyect.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.entities.Speciality;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.SpecialityMapper;
import unimag.proyect.repositories.SpecialityRepository;
import unimag.proyect.services.impl.SpecialityServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialityServiceImplTest {

    @Mock private SpecialityRepository specialityRepository;
    @Mock private SpecialityMapper specialityMapper;

    @InjectMocks
    private SpecialityServiceImpl specialityService;

    private UUID id;
    private Speciality speciality;
    private SpecialityResponse response;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        speciality = new Speciality();
        speciality.setIdSpeciality(id);
        speciality.setName("Psicología");
        response = new SpecialityResponse(id, "Psicología");
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturnResponse_whenNameIsUnique() {
        CreateSpecialtyRequest request = new CreateSpecialtyRequest("Psicología");

        when(specialityRepository.existsByName("Psicología")).thenReturn(false);
        when(specialityMapper.toEntity(request)).thenReturn(speciality);
        when(specialityRepository.save(speciality)).thenReturn(speciality);
        when(specialityMapper.toResponse(speciality)).thenReturn(response);

        SpecialityResponse result = specialityService.create(request);

        assertThat(result).isEqualTo(response);
        verify(specialityRepository).existsByName("Psicología");
        verify(specialityRepository).save(speciality);
        verify(specialityMapper).toResponse(speciality);
    }

    @Test
    void create_shouldThrow_whenNameAlreadyExists() {
        CreateSpecialtyRequest request = new CreateSpecialtyRequest("Psicología");

        when(specialityRepository.existsByName("Psicología")).thenReturn(true);

        assertThatThrownBy(() -> specialityService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(specialityRepository, never()).save(any());
        verifyNoInteractions(specialityMapper);
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenExists() {
        when(specialityRepository.findById(id)).thenReturn(Optional.of(speciality));
        when(specialityMapper.toResponse(speciality)).thenReturn(response);

        SpecialityResponse result = specialityService.findById(id);

        assertThat(result).isEqualTo(response);
        verify(specialityRepository).findById(id);
        verify(specialityMapper).toResponse(speciality);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(specialityRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> specialityService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(specialityMapper);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnMappedList() {
        Speciality s2 = new Speciality();
        s2.setIdSpeciality(UUID.randomUUID());
        s2.setName("Nutrición");
        SpecialityResponse r2 = new SpecialityResponse(s2.getIdSpeciality(), "Nutrición");

        when(specialityRepository.findAll()).thenReturn(List.of(speciality, s2));
        when(specialityMapper.toResponse(speciality)).thenReturn(response);
        when(specialityMapper.toResponse(s2)).thenReturn(r2);

        List<SpecialityResponse> result = specialityService.findAll();

        assertThat(result).hasSize(2).containsExactly(response, r2);
        verify(specialityMapper, times(2)).toResponse(any(Speciality.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoSpecialities() {
        when(specialityRepository.findAll()).thenReturn(List.of());

        List<SpecialityResponse> result = specialityService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(specialityMapper);
    }
}
