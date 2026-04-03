package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import unimag.proyect.dto.request.CreateSpecialtyRequest;
import unimag.proyect.dto.response.SpecialtyResponse;
import unimag.proyect.entities.Speciality; // Ojo a cómo se llama tu entidad ("Speciality")

@Mapper(componentModel = "spring")
public interface SpecialtyMapper {

    Speciality toEntity(CreateSpecialtyRequest request);

    @Mapping(source = "idSpeciality", target = "id")
    SpecialtyResponse toResponse(Speciality speciality);
}