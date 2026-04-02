package unimag.proyect.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorRequest {
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private UUID specialityId;
}
