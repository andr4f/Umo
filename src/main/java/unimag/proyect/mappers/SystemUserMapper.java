package unimag.proyect.mappers;

import org.mapstruct.*;
import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.request.UpdateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;
import unimag.proyect.entities.SystemUser;

@Mapper(componentModel = "spring")
public interface SystemUserMapper {

    @Mapping(target = "idPerson", ignore = true)      // BD lo genera
    @Mapping(target = "role", ignore = true)           // service lo resuelve
    @Mapping(target = "status", ignore = true)         // @Builder.Default = ACTIVE
    @Mapping(target = "password", ignore = true)       // se encripta en service
    @Mapping(target = "phone", ignore = true)          // opcional, no en request
    @Mapping(target = "gender", ignore = true)         // opcional, no en request
    // fullName, documentType, documentNumber, email, username → mapea automático ✅
    SystemUser toEntity(CreateSystemUserRequest request);

    @Mapping(source = "idPerson", target = "idPerson")
    @Mapping(source = "role.name", target = "roleName")
    SystemUserResponse toResponse(SystemUser systemUser);

    @Mapping(target = "idPerson", ignore = true)
    @Mapping(target = "role", ignore = true)           // service lo resuelve
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "documentType", ignore = true)   // no se actualiza
    @Mapping(target = "documentNumber", ignore = true) // no se actualiza
    @Mapping(target = "username", ignore = true)       // cambio delicado — endpoint aparte
    // fullName, email → actualizan automático ✅
    void updateEntity(UpdateSystemUserRequest request, @MappingTarget SystemUser systemUser);
}