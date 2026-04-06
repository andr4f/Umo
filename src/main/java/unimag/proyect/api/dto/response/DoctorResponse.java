package unimag.proyect.api.dto.response;

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
    UUID specialityId,      // útil si el frontend necesita filtrar
    String specialityName,  // útil para mostrar en pantalla
    PersonStatus status
) {}