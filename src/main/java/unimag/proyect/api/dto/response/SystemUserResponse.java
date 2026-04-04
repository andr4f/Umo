package unimag.proyect.api.dto.response;

import java.util.UUID;

public record SystemUserResponse(
    UUID idPerson,
    String fullName,       // ← igual que en Person
    String username,
    String roleName
) {}