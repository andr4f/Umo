package unimag.proyect.services;

import unimag.proyect.api.dto.response.RoleResponse;
import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponse findById(UUID id);
    List<RoleResponse> findAll();
}