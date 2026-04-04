package unimag.proyect.api.dto.response.reports;

import java.util.UUID;

public record DoctorProductivityResponse(
        UUID doctorId,
        String doctorName,
        Long completedAppointments
) {}