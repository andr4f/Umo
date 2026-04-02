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
public class OfficeResponse {
    private UUID idOffice;
    private String code;
    private String name;
    private String location;
    private String status;
}
