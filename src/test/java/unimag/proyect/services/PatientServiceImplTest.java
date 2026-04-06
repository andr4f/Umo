package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.PatientMapper;
import unimag.proyect.repositories.PatientRepository;
import unimag.proyect.services.impl.PatientServiceImpl;
import unimag.proyect.enums.Gender;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private PatientMapper     patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private UUID      patientId;
    private Patient   patient;
    private PatientResponse response;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();

        patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setFullName("Juan Pérez");
        patient.setEmail("juan@unimag.edu");
        patient.setDocumentNumber("123456789");
        patient.setStatus(PersonStatus.ACTIVE);

        response = new PatientResponse(
        patientId,
        "Juan Pérez",
        "CC",
        "123456789",
        "juan@unimag.edu",
        "3001234567",
        Gender.MALE
    );
        }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveAndReturn_whenEmailAndDocumentAreUnique() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Juan Pérez", "CC", "123456789", "juan@unimag.edu", null, null
        );

        when(patientRepository.findByEmail("juan@unimag.edu"))
                .thenReturn(Optional.empty());
        when(patientRepository.findByDocumentNumber("123456789"))
                .thenReturn(Optional.empty());
        when(patientMapper.toEntity(request)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        PatientResponse result = patientService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(patient.getStatus()).isEqualTo(PersonStatus.ACTIVE);
        verify(patientRepository).save(patient);
    }

    @Test
    void create_shouldThrow_whenEmailAlreadyInUse() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Juan Pérez", "CC", "123456789", "juan@unimag.edu", null, null
        );

        // findByEmail devuelve un paciente existente → ifPresent lanza excepción
        when(patientRepository.findByEmail("juan@unimag.edu"))
                .thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(patientRepository, never()).findByDocumentNumber(any());
        verify(patientRepository, never()).save(any());
        verifyNoInteractions(patientMapper);
    }

    @Test
    void create_shouldThrow_whenDocumentNumberAlreadyInUse() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Juan Pérez", "CC", "123456789", "juan@unimag.edu", null, null
        );

        when(patientRepository.findByEmail("juan@unimag.edu"))
                .thenReturn(Optional.empty());
        when(patientRepository.findByDocumentNumber("123456789"))
                .thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(patientRepository, never()).save(any());
        verifyNoInteractions(patientMapper);
    }

    @Test
    void create_shouldSetStatusActive_regardlessOfRequest() {
        CreatePatientRequest request = new CreatePatientRequest(
                "Juan Pérez", "CC", "123456789", "juan@unimag.edu", null, null
        );

        when(patientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(patientRepository.findByDocumentNumber(any())).thenReturn(Optional.empty());
        when(patientMapper.toEntity(request)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        patientService.create(request);

        // el service siempre fuerza ACTIVE — el cliente no lo controla
        assertThat(patient.getStatus()).isEqualTo(PersonStatus.ACTIVE);
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenExists() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(response);

        assertThat(patientService.findById(patientId)).isEqualTo(response);
        verify(patientMapper).toResponse(patient);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(patientId))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(patientMapper);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────
 @Test
    void findAll_shouldReturnMappedList() {
        Patient p2 = new Patient();
        p2.setIdPerson(UUID.randomUUID());
        PatientResponse r2 = new PatientResponse(
        p2.getIdPerson(),
        "Ana López",
        "TI",
        "987654321",
        "ana@unimag.edu",
        "3019876543",
        Gender.FEMALE
    );
        when(patientRepository.findAll()).thenReturn(List.of(patient, p2));
        when(patientMapper.toResponse(patient)).thenReturn(response);
        when(patientMapper.toResponse(p2)).thenReturn(r2);

        List<PatientResponse> result = patientService.findAll();

        assertThat(result).hasSize(2).containsExactly(response, r2);
        verify(patientMapper, times(2)).toResponse(any(Patient.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoPatients() {
        when(patientRepository.findAll()).thenReturn(List.of());

        assertThat(patientService.findAll()).isEmpty();
        verifyNoInteractions(patientMapper);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldMutateAndSave_whenPatientExists() {
        UpdatePatientRequest request = new UpdatePatientRequest(
        "Juan Actualizado",
        "juan.actualizado@unimag.edu",
        "3005550001",
        Gender.MALE,
        PersonStatus.ACTIVE
    );
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        PatientResponse result = patientService.update(patientId, request);

        assertThat(result).isEqualTo(response);
        verify(patientMapper).updateEntity(patient, request);
        verify(patientRepository).save(patient);
    }

    @Test
    void update_shouldThrow_whenPatientNotFound() {
       UpdatePatientRequest request = new UpdatePatientRequest(
        "Juan Actualizado",
        "juan.actualizado@unimag.edu",
        "3005550001",
        Gender.MALE,
        PersonStatus.ACTIVE
    );

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.update(patientId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(patientMapper, never()).updateEntity(any(), any());
        verify(patientRepository, never()).save(any());
    }
}