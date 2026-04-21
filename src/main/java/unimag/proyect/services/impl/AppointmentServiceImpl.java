package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import unimag.proyect.exceptions.InactiveEntityException;
import unimag.proyect.exceptions.InvalidDateRangeException;
import unimag.proyect.exceptions.InvalidStateTransitionException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.exceptions.ScheduleConflictException;
import unimag.proyect.repositories.*;
import unimag.proyect.services.AppointmentService;
import unimag.proyect.mappers.AppointmentMapper;
import unimag.proyect.mappers.WeekDayMapper;

import java.time.LocalDateTime;
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
            throw new InvalidDateRangeException("Appointment cannot be in the past");
        }

        Patient patient = findActivePatient(request.patientId());
        Doctor doctor = findActiveDoctor(request.doctorId());
        Office office = findActiveOffice(request.officeId());

        AppointmentType type = appointmentTypeRepository.findById(request.appointmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType", request.appointmentTypeId()));

        LocalDateTime start = request.startTime();
        LocalDateTime end = start.plusMinutes(type.getDuration());

        WeekDay weekDay = WeekDayMapper.from(start.getDayOfWeek());

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(doctor.getIdPerson(), weekDay);

        boolean fitsSchedule = schedules.stream().anyMatch(s ->
                !start.toLocalTime().isBefore(s.getStartTime())
                        && !end.toLocalTime().isAfter(s.getEndTime())
        );

        if (!fitsSchedule) {
            throw new ScheduleConflictException("Doctor", "appointment is outside doctor's working hours");
        }

        boolean doctorConflict = appointmentRepository.existsDoctorConflict(
                doctor.getIdPerson(), start, end);
        if (doctorConflict) {
            throw new ScheduleConflictException("Doctor", "already has an appointment in this time range");
        }

        boolean officeConflict = appointmentRepository.existsOfficeConflict(
                office.getIdOffice(), start, end);
        if (officeConflict) {
            throw new ScheduleConflictException("Office", "already has an appointment in this time range");
        }

        boolean patientConflict = appointmentRepository.existsPatientConflict(
                patient.getIdPerson(), start, end);
        if (patientConflict) {
            throw new ScheduleConflictException(
                    "Patient", "already has an active appointment in this time range");
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
    public Page<AppointmentResponse> findAll(Pageable pageable) {
        return appointmentRepository.findAllWithDetails(pageable)
                .map(appointmentMapper::toResponse);
    }

    @Override
    public AppointmentResponse confirm(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidStateTransitionException("Appointment", appointment.getStatus().name(), "CONFIRMED");
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
            throw new InvalidStateTransitionException("Appointment", appointment.getStatus().name(), "CANCELLED");
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
            throw new InvalidStateTransitionException("Appointment", appointment.getStatus().name(), "COMPLETED");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartTime())) {
            throw new InvalidDateRangeException("Appointment cannot be completed before it starts");
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
            throw new InvalidStateTransitionException("Appointment", appointment.getStatus().name(), "NO_SHOW");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(appointment.getStartTime())) {
            throw new InvalidDateRangeException("Appointment cannot be marked NO_SHOW before it starts");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    private Patient findActivePatient(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        if (patient.getStatus() != PersonStatus.ACTIVE) {
            throw new InactiveEntityException("Patient", id);
        }
        return patient;
    }

    private Doctor findActiveDoctor(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        if (doctor.getStatus() != PersonStatus.ACTIVE) {
            throw new InactiveEntityException("Doctor", id);
        }
        return doctor;
    }

    private Office findActiveOffice(UUID id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
        if (office.getStatus() != OfficeStatus.ACTIVE) {
            throw new InactiveEntityException("Office", id);
        }
        return office;
    }
}