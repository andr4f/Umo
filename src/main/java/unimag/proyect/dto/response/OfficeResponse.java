package unimag.proyect.dto.response;

import java.util.UUID;
import unimag.proyect.enums.OfficeStatus;

public record OfficeResponse(
        UUID id,
        String code,
        String name,
        String location,
        OfficeStatus status
) {}