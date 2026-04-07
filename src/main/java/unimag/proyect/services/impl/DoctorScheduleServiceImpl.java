package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.DoctorScheduleRepository;
import unimag.proyect.services.DoctorScheduleService;
import unimag.proyect.mappers.DoctorScheduleMapper;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;

    @Override
    public DoctorScheduleResponse create(UUID doctorId, CreateDoctorScheduleRequest request) {
        if (request.startTime().isAfter(request.endTime())
                || request.startTime().equals(request.endTime())) {
            throw new BusinessException("Schedule start time must be before end time");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        boolean conflict = doctorScheduleRepository.existsScheduleConflict(
                doctorId,
                request.weekDay(),
                request.startTime(),
                request.endTime()
        );
        if (conflict) {
            throw new ConflictException("Doctor already has schedule in this time range");
        }

        DoctorSchedule schedule = doctorScheduleMapper.toEntity(request);
        schedule.setDoctor(doctor);
        schedule.setStatus(ScheduleStatus.AVAILABLE);

        DoctorSchedule saved = doctorScheduleRepository.save(schedule);
        return doctorScheduleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> findByDoctor(UUID doctorId) {
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctor_IdPerson(doctorId);
        return schedules.stream()
                .map(doctorScheduleMapper::toResponse)
                .toList();
    }
}