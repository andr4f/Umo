package unimag.proyect.dto.response;

import java.util.UUID;

public record DoctorProductivityResponse(
        UUID doctorId,
        String doctorName,
        Long completedAppointments
) {}