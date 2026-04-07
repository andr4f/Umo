package unimag.proyect.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSpecialtyRequest(
        @NotBlank(message = "Specialty name is required")
        String name
) {}