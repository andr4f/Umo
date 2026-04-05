package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Speciality;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.DoctorMapper;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.SpecialityRepository;
import unimag.proyect.services.impl.DoctorServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialityRepository specialityRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private UUID doctorId;
    private UUID specialityId;
    private Doctor doctor;
    private Speciality speciality;
    private DoctorResponse response;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        specialityId = UUID.randomUUID();

        speciality = new Speciality();
        speciality.setIdSpeciality(specialityId);
        speciality.setName("Psicología");

        doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setFullName("Dr. Carlos Pérez");
        doctor.setDocumentType("CC");
        doctor.setDocumentNumber("123456789");
        doctor.setEmail("carlos@unimag.edu");
        doctor.setPhone("3001112233");
        doctor.setGender(Gender.MALE);
        doctor.setRegisterNum("RM-001");
        doctor.setStatus(PersonStatus.ACTIVE);
        doctor.setSpeciality(speciality);

        response = new DoctorResponse(
                doctorId,
                "Dr. Carlos Pérez",
                "CC",
                "123456789",
                "carlos@unimag.edu",
                "3001112233",
                Gender.MALE,
                "RM-001",
                specialityId,
                "Psicología",
                PersonStatus.ACTIVE
        );
    }

    @Test
    void create_shouldSaveAndReturn_whenValid() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Dr. Carlos Pérez",
                "CC",
                "123456789",
                "carlos@unimag.edu",
                "3001112233",
                Gender.MALE,
                "RM-001",
                specialityId
        );

        when(doctorRepository.findByEmail("carlos@unimag.edu"))
                .thenReturn(Optional.empty());
        when(doctorRepository.findByRegisterNum("RM-001"))
                .thenReturn(Optional.empty());
        when(specialityRepository.findById(specialityId))
                .thenReturn(Optional.of(speciality));
        when(doctorMapper.toEntity(request)).thenReturn(doctor);
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(doctor.getStatus()).isEqualTo(PersonStatus.ACTIVE);
        assertThat(doctor.getSpeciality()).isEqualTo(speciality);

        verify(doctorRepository).findByEmail("carlos@unimag.edu");
        verify(doctorRepository).findByRegisterNum("RM-001");
        verify(specialityRepository).findById(specialityId);
        verify(doctorMapper).toEntity(request);
        verify(doctorRepository).save(doctor);
        verify(doctorMapper).toResponse(doctor);
    }

    @Test
    void create_shouldThrow_whenEmailAlreadyInUse() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Dr. Carlos Pérez",
                "CC",
                "123456789",
                "carlos@unimag.edu",
                "3001112233",
                Gender.MALE,
                "RM-001",
                specialityId
        );

        when(doctorRepository.findByEmail("carlos@unimag.edu"))
                .thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(doctorRepository, never()).findByRegisterNum(any());
        verifyNoInteractions(specialityRepository, doctorMapper);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenRegisterNumberAlreadyInUse() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Dr. Carlos Pérez",
                "CC",
                "123456789",
                "carlos@unimag.edu",
                "3001112233",
                Gender.MALE,
                "RM-001",
                specialityId
        );

        when(doctorRepository.findByEmail("carlos@unimag.edu"))
                .thenReturn(Optional.empty());
        when(doctorRepository.findByRegisterNum("RM-001"))
                .thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(specialityRepository, doctorMapper);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenSpecialityNotFound() {
        CreateDoctorRequest request = new CreateDoctorRequest(
                "Dr. Carlos Pérez",
                "CC",
                "123456789",
                "carlos@unimag.edu",
                "3001112233",
                Gender.MALE,
                "RM-001",
                specialityId
        );

        when(doctorRepository.findByEmail("carlos@unimag.edu"))
                .thenReturn(Optional.empty());
        when(doctorRepository.findByRegisterNum("RM-001"))
                .thenReturn(Optional.empty());
        when(specialityRepository.findById(specialityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(doctorMapper);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnResponse_whenDoctorExists() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.findById(doctorId);

        assertThat(result).isEqualTo(response);
        verify(doctorRepository).findById(doctorId);
        verify(doctorMapper).toResponse(doctor);
    }

    @Test
    void findById_shouldThrow_whenDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.findById(doctorId))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(doctorMapper);
    }

    @Test
    void findAll_shouldReturnOnlyActiveDoctorsMapped() {
        Doctor doctor2 = new Doctor();
        UUID doctor2Id = UUID.randomUUID();
        doctor2.setIdPerson(doctor2Id);
        doctor2.setFullName("Dra. Laura Gómez");
        doctor2.setStatus(PersonStatus.ACTIVE);

        DoctorResponse response2 = new DoctorResponse(
                doctor2Id,
                "Dra. Laura Gómez",
                "CC",
                "987654321",
                "laura@unimag.edu",
                "3004445566",
                Gender.FEMALE,
                "RM-002",
                specialityId,
                "Psicología",
                PersonStatus.ACTIVE
        );

        when(doctorRepository.findByStatus(PersonStatus.ACTIVE))
                .thenReturn(List.of(doctor, doctor2));
        when(doctorMapper.toResponse(doctor)).thenReturn(response);
        when(doctorMapper.toResponse(doctor2)).thenReturn(response2);

        List<DoctorResponse> result = doctorService.findAll();

        assertThat(result).hasSize(2).containsExactly(response, response2);
        verify(doctorRepository).findByStatus(PersonStatus.ACTIVE);
        verify(doctorMapper, times(2)).toResponse(any(Doctor.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoActiveDoctors() {
        when(doctorRepository.findByStatus(PersonStatus.ACTIVE))
                .thenReturn(List.of());

        List<DoctorResponse> result = doctorService.findAll();

        assertThat(result).isEmpty();
        verify(doctorRepository).findByStatus(PersonStatus.ACTIVE);
        verifyNoInteractions(doctorMapper);
    }

    @Test
    void update_shouldMutateSaveAndReturn_whenValid() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "Dr. Carlos Actualizado",
                "carlos.actualizado@unimag.edu",
                "3009998877",
                Gender.MALE,
                "RM-001-UPDATED",
                specialityId
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialityRepository.findById(specialityId)).thenReturn(Optional.of(speciality));
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        when(doctorMapper.toResponse(doctor)).thenReturn(response);

        DoctorResponse result = doctorService.update(doctorId, request);

        assertThat(result).isEqualTo(response);
        assertThat(doctor.getSpeciality()).isEqualTo(speciality);

        verify(doctorMapper).updateEntity(doctor, request);
        verify(doctorRepository).save(doctor);
        verify(doctorMapper).toResponse(doctor);
    }

    @Test
    void update_shouldThrow_whenDoctorNotFound() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "Dr. Carlos Actualizado",
                "carlos.actualizado@unimag.edu",
                "3009998877",
                Gender.MALE,
                "RM-001-UPDATED",
                specialityId
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.update(doctorId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(specialityRepository, doctorMapper);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void update_shouldThrow_whenSpecialityNotFound() {
        UpdateDoctorRequest request = new UpdateDoctorRequest(
                "Dr. Carlos Actualizado",
                "carlos.actualizado@unimag.edu",
                "3009998877",
                Gender.MALE,
                "RM-001-UPDATED",
                specialityId
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialityRepository.findById(specialityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.update(doctorId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorMapper, never()).updateEntity(any(), any());
        verify(doctorRepository, never()).save(any());
    }
}