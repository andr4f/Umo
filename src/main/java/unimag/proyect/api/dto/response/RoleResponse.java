package unimag.proyect.api.dto.response;

import java.util.UUID;

public record
RoleResponse(
        UUID id,
        String name
) {}