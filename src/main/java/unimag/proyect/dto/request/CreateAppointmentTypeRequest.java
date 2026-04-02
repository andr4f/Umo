package unimag.proyect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Duration is required")
    private Integer duration;
}
