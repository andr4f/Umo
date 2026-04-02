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
public class PatientResponse {
    private UUID idPerson;
    private String fullName;
    private String documentType;
    private String documentNumber;
    private String email;
    private String phone;
    private String status;
}
