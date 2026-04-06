package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOfficeRequest(
        @NotBlank(message = "Code is required")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        String location
) {}