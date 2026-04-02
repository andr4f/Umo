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
public class OfficeOccupancyResponse {
    private UUID officeId;
    private String officeName;
    private String currentStatus;
    private int currentOccupancy;
}
