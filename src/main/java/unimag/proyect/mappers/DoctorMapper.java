package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import unimag.proyect.dto.request.CreateDoctorRequest;
import unimag.proyect.dto.request.UpdateDoctorRequest;
import unimag.proyect.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    // Omitimos la especialidad aquí porque se debe buscar en BD en el Service
    @Mapping(target = "speciality", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);

    @Mapping(source = "idPerson", target = "id")
    @Mapping(source = "speciality.idSpeciality", target = "specialityId")
    @Mapping(source = "speciality.name", target = "specialityName")
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "speciality", ignore = true)
    void updateEntity(@MappingTarget Doctor doctor, UpdateDoctorRequest request);
}