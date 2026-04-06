package unimag.proyect.api.dto.response;

import java.util.UUID;

public record SpecialityResponse(
        UUID id,
        String name
) {}