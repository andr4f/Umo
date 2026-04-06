package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.PersonStatus;

public record UpdatePersonStatusRequest(
    @NotNull(message = "Status is required")
    PersonStatus status
) {}