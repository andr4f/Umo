package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import unimag.proyect.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.dto.response.AppointmentTypeResponse;
import unimag.proyect.entities.AppointmentType;

@Mapper(componentModel = "spring")
public interface AppointmentTypeMapper {

    @Mapping(source = "durationMinutes", target = "duration")
    AppointmentType toEntity(CreateAppointmentTypeRequest request);

    @Mapping(source = "idAppointmentType", target = "id")
    @Mapping(source = "duration", target = "durationMinutes")
    AppointmentTypeResponse toResponse(AppointmentType appointmentType);
}