package unimag.proyect.api.dto.response;

import java.util.UUID;
import unimag.proyect.enums.Gender;

public record PatientResponse(
        UUID id,
        String fullName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        Gender gender
) {}