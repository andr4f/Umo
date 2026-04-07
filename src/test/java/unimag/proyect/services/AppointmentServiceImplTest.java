package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.entities.*;
import unimag.proyect.enums.*;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.repositories.*;
import unimag.proyect.services.impl.AppointmentServiceImpl;
import unimag.proyect.mappers.AppointmentMapper;
import unimag.proyect.mappers.WeekDayMapper;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private OfficeRepository officeRepository;
    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;
    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl service;

    private UUID patientId;
    private UUID doctorId;
    private UUID officeId;
    private UUID typeId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        officeId = UUID.randomUUID();
        typeId = UUID.randomUUID();
    }

    @Test
    void create_shouldRejectAppointmentInPast() {
        LocalDateTime past = LocalDateTime.now().minusHours(1);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                patientId, doctorId, officeId, typeId, past
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("past");
    }
    @Test
    void create_shouldRejectWhenDoctorScheduleDoesNotCoverTime() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                patientId, doctorId, officeId, typeId, start
        );

        Patient patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setStatus(PersonStatus.ACTIVE);

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setStatus(PersonStatus.ACTIVE);

        Office office = new Office();
        office.setIdOffice(officeId);
        office.setStatus(OfficeStatus.ACTIVE);

        AppointmentType type = new AppointmentType();
        type.setIdAppointmentType(typeId);
        type.setDuration(30);

        // Horario que definitivamente NO cubre el start (siempre 01:00-02:00 AM)
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setWeekDay(WeekDayMapper.from(start.getDayOfWeek())); // día correcto
        schedule.setStartTime(LocalTime.of(1, 0));
        schedule.setEndTime(LocalTime.of(2, 0));

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(any(), any()))
                .thenReturn(List.of(schedule));

        // SIN stub de appointmentMapper.toEntity — la excepción ocurre antes

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("working hours");
    }

    @Test
    void create_shouldRejectWhenDoctorHasConflict() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                patientId, doctorId, officeId, typeId, start
        );

        Patient patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setStatus(PersonStatus.ACTIVE);

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setStatus(PersonStatus.ACTIVE);

        Office office = new Office();
        office.setIdOffice(officeId);
        office.setStatus(OfficeStatus.ACTIVE);

        AppointmentType type = new AppointmentType();
        type.setIdAppointmentType(typeId);
        type.setDuration(30);

        // Horario que SÍ cubre el start — día dinámico
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setWeekDay(toWeekDay(start.getDayOfWeek()));
        schedule.setStartTime(LocalTime.of(0, 0));
        schedule.setEndTime(LocalTime.of(23, 59));

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsDoctorConflict(eq(doctorId), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Doctor already has an appointment");
    }

    @Test
    void create_shouldRejectWhenOfficeHasConflict() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                patientId, doctorId, officeId, typeId, start
        );

        Patient patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setStatus(PersonStatus.ACTIVE);

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setStatus(PersonStatus.ACTIVE);

        Office office = new Office();
        office.setIdOffice(officeId);
        office.setStatus(OfficeStatus.ACTIVE);

        AppointmentType type = new AppointmentType();
        type.setIdAppointmentType(typeId);
        type.setDuration(30);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setWeekDay(toWeekDay(start.getDayOfWeek())); // día dinámico
        schedule.setStartTime(LocalTime.of(0, 0));
        schedule.setEndTime(LocalTime.of(23, 59));

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsDoctorConflict(eq(doctorId), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.existsOfficeConflict(eq(officeId), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Office already has an appointment");
    }

    @Test
    void create_shouldCalculateEndTimeCorrectly_whenValid() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                patientId, doctorId, officeId, typeId, start
        );

        Patient patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setStatus(PersonStatus.ACTIVE);

        Doctor doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setStatus(PersonStatus.ACTIVE);

        Office office = new Office();
        office.setIdOffice(officeId);
        office.setStatus(OfficeStatus.ACTIVE);

        AppointmentType type = new AppointmentType();
        type.setIdAppointmentType(typeId);
        type.setDuration(45);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setWeekDay(toWeekDay(start.getDayOfWeek())); // día dinámico
        schedule.setStartTime(LocalTime.of(0, 0));
        schedule.setEndTime(LocalTime.of(23, 59));

        Appointment mapped = new Appointment();

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(officeRepository.findById(officeId)).thenReturn(Optional.of(office));
        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(any(), any()))
                .thenReturn(List.of(schedule));
        when(appointmentRepository.existsDoctorConflict(eq(doctorId), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.existsOfficeConflict(eq(officeId), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.existsPatientConflict(eq(patientId), any(), any()))
                .thenReturn(false);
        when(appointmentMapper.toEntity(request)).thenReturn(mapped);
        when(appointmentMapper.toResponse(any())).thenReturn(mock(AppointmentResponse.class));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        when(appointmentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.create(request);

        Appointment saved = captor.getValue();
        assertThat(saved.getEndTime()).isEqualTo(start.plusMinutes(45));
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(saved.getPatient()).isEqualTo(patient);
        assertThat(saved.getDoctor()).isEqualTo(doctor);
        assertThat(saved.getOffice()).isEqualTo(office);
        assertThat(saved.getAppointmentType()).isEqualTo(type);
    }

    @Test
    void cancel_shouldChangeStatusToCancelled_whenScheduled() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.toResponse(any())).thenReturn(mock(AppointmentResponse.class));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Patient requested");

        service.cancel(id, request);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointment.getCancelReason()).isEqualTo("Patient requested");
    }

    @Test
    void cancel_shouldFail_whenCompleted() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancel(id, new CancelAppointmentRequest("x")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    void complete_shouldChangeStatusToCompleted_whenConfirmedAndPast() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().minusMinutes(30));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.toResponse(any())).thenReturn(mock(AppointmentResponse.class));

        service.complete(id, "ok");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(appointment.getObservations()).isEqualTo("ok");
    }

    @Test
    void complete_shouldFail_whenBeforeStartTime() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().plusMinutes(30));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.complete(id, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("before it starts");
    }

    @Test
    void markNoShow_shouldChangeStatusToNoShow_whenConfirmedAndPast() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setStartTime(LocalDateTime.now().minusMinutes(30));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentMapper.toResponse(any())).thenReturn(mock(AppointmentResponse.class));

        service.markNoShow(id);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    @Test
    void markNoShow_shouldFail_whenNotConfirmed() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(id);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartTime(LocalDateTime.now().minusMinutes(30));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.markNoShow(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CONFIRMED");
    }
// Pégalo al final de AppointmentServiceImplTest, antes del cierre de clase
private WeekDay toWeekDay(DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
        case MONDAY    -> WeekDay.MONDAY;
        case TUESDAY   -> WeekDay.TUESDAY;
        case WEDNESDAY -> WeekDay.WEDNESDAY;
        case THURSDAY  -> WeekDay.THURSDAY;
        case FRIDAY    -> WeekDay.FRIDAY;
        case SATURDAY  -> WeekDay.SATURDAY;
        case SUNDAY    -> WeekDay.SUNDAY;
    };
}
}