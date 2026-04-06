package unimag.proyect.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.entities.DoctorSchedule;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {

    @Mapping(target = "id", ignore = true)       // BD lo genera
    @Mapping(target = "doctor", ignore = true)   // service lo resuelve
    @Mapping(target = "status", ignore = true)   // default AVAILABLE
    DoctorSchedule toEntity(CreateDoctorScheduleRequest request);

    @Mapping(source = "doctor.idPerson", target = "doctorId")
    DoctorScheduleResponse toResponse(DoctorSchedule doctorSchedule);
}