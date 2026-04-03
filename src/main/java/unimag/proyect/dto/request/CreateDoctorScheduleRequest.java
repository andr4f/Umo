package unimag.proyect.dto.request;

import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.WeekDay;

import java.time.LocalTime;
import java.util.UUID;

public record CreateDoctorScheduleRequest(
        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotNull(message = "Week day is required")
        WeekDay weekDay,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {}