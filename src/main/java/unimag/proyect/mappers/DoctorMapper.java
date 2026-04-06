package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.request.UpdateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.entities.Doctor;

@Mapper(
    componentModel = "spring"
    // ← eliminar uses = {SpecialityMapper.class}, ya no es necesario
)
public interface DoctorMapper {

    @Mapping(target = "idPerson", ignore = true)
    @Mapping(target = "speciality", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "status", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);

    @Mapping(source = "idPerson", target = "id")
    @Mapping(source = "speciality.idSpeciality", target = "specialityId")   // ← nuevo
    @Mapping(source = "speciality.name", target = "specialityName")         // ← nuevo
    DoctorResponse toResponse(Doctor doctor);

    @Mapping(target = "idPerson", ignore = true)
    @Mapping(target = "speciality", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "documentNumber", ignore = true)
    void updateEntity(@MappingTarget Doctor doctor, UpdateDoctorRequest request);
}