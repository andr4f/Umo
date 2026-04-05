package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.entities.*;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.enums.WeekDay;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.*;
import unimag.proyect.services.AppointmentService;
import unimag.proyect.mappers.AppointmentMapper;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public AppointmentResponse create(CreateAppointmentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (request.startTime().isBefore(now)) {
            throw new BusinessException("Appointment cannot be in the past");
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));
        if (patient.getStatus() != PersonStatus.ACTIVE) {
            throw new BusinessException("Patient must be ACTIVE");
        }

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));
        if (doctor.getStatus() != PersonStatus.ACTIVE) {
            throw new BusinessException("Doctor must be ACTIVE");
        }

        Office office = officeRepository.findById(request.officeId())
                .orElseThrow(() -> new ResourceNotFoundException("Office", request.officeId()));
        if (office.getStatus() != OfficeStatus.ACTIVE) {
            throw new BusinessException("Office must be ACTIVE");
        }

        AppointmentType type = appointmentTypeRepository.findById(request.appointmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType", request.appointmentTypeId()));

        LocalDateTime start = request.startTime();
        LocalDateTime end = start.plusMinutes(type.getDuration());

        WeekDay weekDay = toWeekDay(start.getDayOfWeek());

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(doctor.getIdPerson(), weekDay);

        boolean fitsSchedule = schedules.stream().anyMatch(s ->
                !s.getStartTime().isAfter(end.toLocalTime())
                        && !s.getEndTime().isBefore(start.toLocalTime())
                        && !start.toLocalTime().isBefore(s.getStartTime())
                        && !end.toLocalTime().isAfter(s.getEndTime())
        );
        if (!fitsSchedule) {
            throw new BusinessException("Appointment must be inside doctor's working hours");
        }

        boolean doctorConflict = appointmentRepository.existsDoctorConflict(
                doctor.getIdPerson(), start, end);
        if (doctorConflict) {
            throw new ConflictException("Doctor already has an appointment in this time range");
        }

        boolean officeConflict = appointmentRepository.existsOfficeConflict(
                office.getIdOffice(), start, end);
        if (officeConflict) {
            throw new ConflictException("Office already has an appointment in this time range");
        }

        List<Appointment> patientAppointments =
                appointmentRepository.findByPatient_IdPerson(patient.getIdPerson());
        boolean patientConflict = patientAppointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.NO_SHOW)
                .anyMatch(a -> overlaps(a.getStartTime(), a.getEndTime(), start, end));
        if (patientConflict) {
            throw new ConflictException("Patient already has an active appointment in this time range");
        }

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setOffice(office);
        appointment.setAppointmentType(type);
        appointment.setEndTime(end);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Override
    public AppointmentResponse confirm(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    public AppointmentResponse cancel(UUID id, CancelAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only SCHEDULED or CONFIRMED appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request.cancelReason());

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    public AppointmentResponse complete(UUID id, String observations) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED appointments can be completed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartTime())) {
            throw new BusinessException("Appointment cannot be completed before it starts");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setObservations(observations);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    public AppointmentResponse markNoShow(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED appointments can be marked as NO_SHOW");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartTime())) {
            throw new BusinessException("Appointment cannot be marked NO_SHOW before it starts");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    private boolean overlaps(LocalDateTime start1, LocalDateTime end1,
                             LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private WeekDay toWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> WeekDay.MONDAY;
            case TUESDAY -> WeekDay.TUESDAY;
            case WEDNESDAY -> WeekDay.WEDNESDAY;
            case THURSDAY -> WeekDay.THURSDAY;
            case FRIDAY -> WeekDay.FRIDAY;
            case SATURDAY -> WeekDay.SATURDAY;
            case SUNDAY -> WeekDay.SUNDAY;
        };
    }
}