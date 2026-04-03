package unimag.proyect.dto.response;

import java.util.UUID;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;

public record DoctorResponse(
        UUID id,
        String fullName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        Gender gender,
        String registerNum,
        UUID specialityId,
        String specialityName,
        PersonStatus status
) {}