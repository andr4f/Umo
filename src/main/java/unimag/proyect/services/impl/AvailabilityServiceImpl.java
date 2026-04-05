package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.enums.WeekDay;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.AppointmentTypeRepository;
import unimag.proyect.repositories.DoctorScheduleRepository;
import unimag.proyect.services.AvailabilityService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentTypeRepository appointmentTypeRepository;

    @Override
    public List<AvailabilitySlotResponse> getAvailableSlots(UUID doctorId,
                                                            LocalDate date,
                                                            UUID appointmentTypeId) {
        AppointmentType type = appointmentTypeRepository.findById(appointmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentType", appointmentTypeId));

        int durationMinutes = type.getDuration();
        WeekDay weekDay = toWeekDay(date.getDayOfWeek());

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(doctorId, weekDay);

        List<AvailabilitySlotResponse> slots = new ArrayList<>();

        for (DoctorSchedule s : schedules) {
            LocalTime slotStart = s.getStartTime();
            while (slotStart.plusMinutes(durationMinutes).compareTo(s.getEndTime()) <= 0) {
                LocalDateTime startDateTime = LocalDateTime.of(date, slotStart);
                LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);

                boolean conflict = appointmentRepository.existsDoctorConflict(
                        doctorId, startDateTime, endDateTime);

                if (!conflict) {
                    slots.add(new AvailabilitySlotResponse(startDateTime, endDateTime));
                }

                slotStart = slotStart.plusMinutes(durationMinutes);
            }
        }

        return slots;
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