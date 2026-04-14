package unimag.proyect.api.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

public record UpdatePatientRequest(
    @Nullable String fullName,
    @Nullable @Email String email,
    @Nullable String phone,
    @Nullable Gender gender,
    @Nullable PersonStatus status
) {}