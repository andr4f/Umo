package unimag.proyect.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import unimag.proyect.api.dto.request.CreatePatientRequest;
import unimag.proyect.api.dto.request.UpdatePatientRequest;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.entities.Patient;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "idPerson", ignore = true)       // BD lo genera
    @Mapping(target = "appointments", ignore = true)   // relación lazy
    @Mapping(target = "status", ignore = true)         // default ACTIVE
    Patient toEntity(CreatePatientRequest request);

   @Mapping(source = "idPerson", target = "id")
    PatientResponse toResponse(Patient patient);
    // fullName, documentType, documentNumber, email,
    // phone, gender, status → automáticos ✅

    // PUT: Actualizar Entidad existente con datos del DTO
    @Mapping(target = "idPerson", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "documentType", ignore = true)    // no se actualiza
    @Mapping(target = "documentNumber", ignore = true)  // no se actualiza
    void updateEntity(@MappingTarget Patient patient, UpdatePatientRequest request);
}