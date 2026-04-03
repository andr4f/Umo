package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import unimag.proyect.dto.request.CreateOfficeRequest;
import unimag.proyect.dto.request.UpdateOfficeRequest;
import unimag.proyect.dto.response.OfficeResponse;
import unimag.proyect.entities.Office;

@Mapper(componentModel = "spring")
public interface OfficeMapper {

    Office toEntity(CreateOfficeRequest request);

    @Mapping(source = "idOffice", target = "id")
    OfficeResponse toResponse(Office office);

    void updateEntity(@MappingTarget Office office, UpdateOfficeRequest request);
}