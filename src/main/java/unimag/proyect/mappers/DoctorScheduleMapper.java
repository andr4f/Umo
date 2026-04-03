package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import unimag.proyect.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.dto.response.DoctorScheduleResponse;
import unimag.proyect.entities.DoctorSchedule;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(target = "doctor", ignore = true) // El doctor se asigna en el Service
    DoctorSchedule toEntity(CreateDoctorScheduleRequest request);

    @Mapping(source = "doctor.idPerson", target = "doctorId")
    DoctorScheduleResponse toResponse(DoctorSchedule doctorSchedule);
}