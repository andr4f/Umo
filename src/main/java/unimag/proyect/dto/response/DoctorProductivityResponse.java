package unimag.proyect.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProductivityResponse {
    private UUID doctorId;
    private String doctorName;
    private long completedAppointmentsCount;
    private long canceledAppointmentsCount;
    private double totalHours;
}
