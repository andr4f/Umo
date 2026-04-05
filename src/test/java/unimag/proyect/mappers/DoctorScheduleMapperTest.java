package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorScheduleMapperTest {

    private final DoctorScheduleMapper mapper = Mappers.getMapper(DoctorScheduleMapper.class);

    @Test
    void toEntity_shouldMapFields_andIgnoreIdDoctorAndStatus() {
        // Arrange
        UUID doctorId = UUID.randomUUID();
        CreateDoctorScheduleRequest request = new CreateDoctorScheduleRequest(
                doctorId,
                WeekDay.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)

        );

        // Act
        DoctorSchedule entity = mapper.toEntity(request);

        // Assert
        assertThat(entity.getId()).isNull();
        assertThat(entity.getWeekDay()).isEqualTo(WeekDay.MONDAY);
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(entity.getDoctor()).isNull();           // lo pone el service
        assertThat(entity.getStatus()).isEqualTo(ScheduleStatus.AVAILABLE);           // AVAILABLE en service
    }

    @Test
    void toResponse_shouldMapAllFields_flattenDoctor() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Doctor doctor = Doctor.builder()
                .idPerson(doctorId)
                .fullName("Dr. Agenda")
                .build();

        DoctorSchedule entity = DoctorSchedule.builder()
                .id(scheduleId)
                .doctor(doctor)
                .weekDay(WeekDay.FRIDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .status(ScheduleStatus.AVAILABLE)
                .build();

        // Act
        DoctorScheduleResponse response = mapper.toResponse(entity);

        // Assert
        assertThat(response.id()).isEqualTo(scheduleId);
        assertThat(response.doctorId()).isEqualTo(doctorId);
        assertThat(response.weekDay()).isEqualTo(WeekDay.FRIDAY);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.status()).isEqualTo(ScheduleStatus.AVAILABLE);
    }
}