package unimag.proyect.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentRequest {
    @NotBlank(message = "Cancel reason is required")
    private String cancelReason;
}
