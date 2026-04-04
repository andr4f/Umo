package unimag.proyect.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.entities.Office;

@Mapper(componentModel = "spring")
public interface OfficeMapper {

    @Mapping(target = "idOffice", ignore = true)  // BD lo genera
    @Mapping(target = "status", ignore = true)    // default ACTIVE
    Office toEntity(CreateOfficeRequest request);

    @Mapping(source = "idOffice", target = "id")
    OfficeResponse toResponse(Office office);

    @Mapping(target = "idOffice", ignore = true)  // nunca se actualiza
    @Mapping(target = "code", ignore = true)      // código único no se cambia
    void updateEntity(@MappingTarget Office office, UpdateOfficeRequest request);
}