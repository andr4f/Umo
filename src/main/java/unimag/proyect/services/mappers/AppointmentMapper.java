package unimag.proyect.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.entities.Appointment;

@Mapper(
    componentModel = "spring",
    uses = {          // ← le dice a MapStruct qué mappers usar para los objetos anidados
        PatientMapper.class,
        DoctorMapper.class,
        OfficeMapper.class,
        AppointmentTypeMapper.class
    }
)
public interface AppointmentMapper {

    // toEntity queda igual — sigue recibiendo UUIDs del cliente
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "office", ignore = true)
    @Mapping(target = "appointmentType", ignore = true)
    Appointment toEntity(CreateAppointmentRequest request);

    // toResponse — solo necesita mapear el id
    // patient, doctor, office, appointmentType los mapea
    // automáticamente usando los mappers declarados en uses = {}
    @Mapping(source = "idAppointment", target = "id")
    AppointmentResponse toResponse(Appointment appointment);
}