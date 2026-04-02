package unimag.proyect.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private UUID idAppointment;
    private PatientResponse patient;
    private DoctorResponse doctor;
    private OfficeResponse office;
    private AppointmentTypeResponse appointmentType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String cancelReason;
    private String observations;
}
