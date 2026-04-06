package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.entities.Speciality; // Ojo a cómo se llama tu entidad ("Speciality")

@Mapper(componentModel = "spring")
public interface SpecialityMapper {

    @Mapping(target = "idSpeciality", ignore = true)  // BD lo genera
    @Mapping(target = "doctors", ignore = true)        // relación lazy
    Speciality toEntity(CreateSpecialtyRequest request);

    @Mapping(source = "idSpeciality", target = "id")
    SpecialityResponse toResponse(Speciality speciality);
}