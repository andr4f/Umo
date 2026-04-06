package unimag.proyect.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.DoctorScheduleRepository;
import unimag.proyect.services.impl.DoctorScheduleServiceImpl;
import unimag.proyect.mappers.DoctorScheduleMapper;

import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DoctorScheduleMapper doctorScheduleMapper;

    @InjectMocks
    private DoctorScheduleServiceImpl service;

    @Test
    void create_shouldFail_whenStartIsAfterOrEqualEnd() {
        UUID doctorId = UUID.randomUUID();

        CreateDoctorScheduleRequest request = new CreateDoctorScheduleRequest(
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(9, 0)
        );

        assertThatThrownBy(() -> service.create(doctorId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldFail_whenDoctorNotFound() {
        UUID doctorId = UUID.randomUUID();
        CreateDoctorScheduleRequest request = new CreateDoctorScheduleRequest(
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        );

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(doctorId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldFail_whenScheduleConflictExists() {
        UUID doctorId = UUID.randomUUID();
        CreateDoctorScheduleRequest request = new CreateDoctorScheduleRequest(
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        );

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setFullName("Dr. Test");
        doctor.setStatus(PersonStatus.ACTIVE);
        doctor.setGender(Gender.MALE);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsScheduleConflict(
                eq(doctorId), eq(WeekDay.MONDAY),
                any(), any())
        ).thenReturn(true);

        assertThatThrownBy(() -> service.create(doctorId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("schedule");
    }

    @Test
    void create_shouldSaveScheduleWithAvailableStatus_whenValid() {
        UUID doctorId = UUID.randomUUID();
        CreateDoctorScheduleRequest request = new CreateDoctorScheduleRequest(
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        );

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setFullName("Dr. Test");
        doctor.setStatus(PersonStatus.ACTIVE);

        DoctorSchedule scheduleMapped = new DoctorSchedule();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.existsScheduleConflict(
                eq(doctorId), eq(WeekDay.MONDAY),
                any(), any())
        ).thenReturn(false);
        when(doctorScheduleMapper.toEntity(request)).thenReturn(scheduleMapped);

        when(doctorScheduleRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        when(doctorScheduleMapper.toResponse(any()))
                .thenReturn(new DoctorScheduleResponse(
                        UUID.randomUUID(),
                        doctorId,
                        WeekDay.MONDAY,
                        request.startTime(),
                        request.endTime(),
                        ScheduleStatus.AVAILABLE
                ));

        DoctorScheduleResponse response = service.create(doctorId, request);

        assertThat(scheduleMapped.getDoctor()).isEqualTo(doctor);
        assertThat(scheduleMapped.getStatus()).isEqualTo(ScheduleStatus.AVAILABLE);

        assertThat(response.doctorId()).isEqualTo(doctorId);
        assertThat(response.status()).isEqualTo(ScheduleStatus.AVAILABLE);
    }

    @Test
    void findByDoctor_shouldReturnMappedResponses() {
        UUID doctorId = UUID.randomUUID();

        DoctorSchedule s1 = new DoctorSchedule();
        s1.setId(UUID.randomUUID());
        s1.setWeekDay(WeekDay.MONDAY);

        when(doctorScheduleRepository.findByDoctor_IdPerson(doctorId))
                .thenReturn(Collections.singletonList(s1));

        DoctorScheduleResponse resp = new DoctorScheduleResponse(
                s1.getId(),
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                ScheduleStatus.AVAILABLE
        );
        when(doctorScheduleMapper.toResponse(s1)).thenReturn(resp);

        var result = service.findByDoctor(doctorId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(s1.getId());
    }
}