package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import unimag.proyect.api.dto.response.RoleResponse;
import unimag.proyect.entities.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toResponse(Role role);
}