package unimag.proyect.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.entities.AppointmentType;

@Mapper(componentModel = "spring")
public interface AppointmentTypeMapper {

    @Mapping(target = "idAppointmentType", ignore = true)  // ← BD lo genera
    @Mapping(target = "appointments", ignore = true)        // ← relación lazy
    @Mapping(source = "durationMinutes", target = "duration")
    AppointmentType toEntity(CreateAppointmentTypeRequest request);

    @Mapping(source = "idAppointmentType", target = "id")
    @Mapping(source = "duration", target = "durationMinutes")
    AppointmentTypeResponse toResponse(AppointmentType appointmentType);
}