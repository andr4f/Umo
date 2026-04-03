package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import unimag.proyect.dto.request.CreatePatientRequest;
import unimag.proyect.dto.request.UpdatePatientRequest;
import unimag.proyect.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    // POST: DTO a Entidad
    Patient toEntity(CreatePatientRequest request);

    // GET: Entidad a DTO
    @Mapping(source = "idPerson", target = "id")
    PatientResponse toResponse(Patient patient);

    // PUT: Actualizar Entidad existente con datos del DTO
    void updateEntity(@MappingTarget Patient patient, UpdatePatientRequest request);
}