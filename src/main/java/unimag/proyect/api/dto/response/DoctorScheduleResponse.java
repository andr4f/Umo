package unimag.proyect.api.dto.response;

import java.time.LocalTime;
import java.util.UUID;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;

public record DoctorScheduleResponse(
        UUID id,
        UUID doctorId,
        WeekDay weekDay,
        LocalTime startTime,
        LocalTime endTime,
        ScheduleStatus status
) {}