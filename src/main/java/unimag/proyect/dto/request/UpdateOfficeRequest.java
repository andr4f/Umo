package unimag.proyect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import unimag.proyect.enums.OfficeStatus;

public record UpdateOfficeRequest(
        @NotBlank(message = "Name is required")
        String name,

        String location,

        @NotNull(message = "Status is required")
        OfficeStatus status
) {}