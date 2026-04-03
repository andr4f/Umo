package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import unimag.proyect.dto.request.CreateAppointmentRequest;
import unimag.proyect.dto.response.AppointmentResponse;
import unimag.proyect.entities.Appointment;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    // Las relaciones se mapean e ignoran porque debes validarlas en BD desde el Service
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "office", ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    Appointment toEntity(CreateAppointmentRequest request);

    @Mapping(source = "idAppointment", target = "id")
    @Mapping(source = "patient.idPerson", target = "patientId")
    @Mapping(source = "doctor.idPerson", target = "doctorId")
    @Mapping(source = "office.idOffice", target = "officeId")
    @Mapping(source = "appointmentType.idAppointmentType", target = "appointmentTypeId")
    AppointmentResponse toResponse(Appointment appointment);
}