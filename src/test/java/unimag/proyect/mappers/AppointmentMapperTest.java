package unimag.proyect.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.entities.*;
import unimag.proyect.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AppointmentMapperImpl.class,
        PatientMapperImpl.class,
        DoctorMapperImpl.class,
        OfficeMapperImpl.class,
        AppointmentTypeMapperImpl.class
})
class AppointmentMapperTest {

    @Autowired
    private AppointmentMapper mapper;

    @Test
    void toEntity_shouldIgnoreAllRelations_andMapStartTime() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 9, 0);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), start
        );

        Appointment entity = mapper.toEntity(request);

        assertThat(entity.getIdAppointment()).isNull();
        assertThat(entity.getPatient()).isNull();
        assertThat(entity.getDoctor()).isNull();
        assertThat(entity.getOffice()).isNull();
        assertThat(entity.getAppointmentType()).isNull();
        assertThat(entity.getStartTime()).isEqualTo(start);
        assertThat(entity.getEndTime()).isNull();
        // @Builder.Default pone SCHEDULED
        assertThat(entity.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void toResponse_shouldMapIdStatusTimesAndNestedObjects() {
        Patient patient = Patient.builder()
                .idPerson(UUID.randomUUID()).fullName("Paciente Test").build();

        Doctor doctor = Doctor.builder()
                .idPerson(UUID.randomUUID()).fullName("Dr. Test").build();

        Office office = Office.builder()
                .idOffice(UUID.randomUUID()).code("OFC-01").name("Consultorio 1").build();

        AppointmentType type = AppointmentType.builder()
                .idAppointmentType(UUID.randomUUID())
                .name("Consulta general").duration(30).build();

        Appointment entity = Appointment.builder()
                .idAppointment(UUID.randomUUID())
                .patient(patient).doctor(doctor)
                .office(office).appointmentType(type)
                .startTime(LocalDateTime.of(2026, 4, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 4, 10, 9, 30))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        AppointmentResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(entity.getIdAppointment());
        assertThat(response.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(response.startTime()).isEqualTo(entity.getStartTime());
        assertThat(response.endTime()).isEqualTo(entity.getEndTime());
        assertThat(response.patient().id()).isEqualTo(patient.getIdPerson());
        assertThat(response.doctor().id()).isEqualTo(doctor.getIdPerson());
        assertThat(response.office().id()).isEqualTo(office.getIdOffice());
        assertThat(response.appointmentType().id()).isEqualTo(type.getIdAppointmentType());
    }
}