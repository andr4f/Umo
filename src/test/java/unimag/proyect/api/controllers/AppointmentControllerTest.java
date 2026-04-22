package unimag.proyect.api.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.api.dto.response.PatientResponse;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.AppointmentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppointmentController.class)
class AppointmentControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void create_returns201() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        String json = String.format(
                "{\"patientId\":\"%s\",\"doctorId\":\"%s\",\"officeId\":\"%s\",\"appointmentTypeId\":\"%s\",\"startTime\":\"2030-06-15T10:00:00\"}",
                patientId, doctorId, officeId, typeId);

        UUID apptId = UUID.randomUUID();
        when(appointmentService.create(any())).thenReturn(sampleAppointment(apptId, patientId, doctorId, officeId, typeId));

        postRawJson("/api/v1/appointments", json)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService).create(any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        postRawJson("/api/v1/appointments", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.findById(id)).thenReturn(sampleAppointment(id));

        getJson("/api/v1/appointments/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.toString()));

        verify(appointmentService).findById(eq(id));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.findById(id)).thenThrow(new ResourceNotFoundException("Appointment", id));

        getJson("/api/v1/appointments/" + id).andExpect(status().isNotFound());
    }

    @Test
    void list_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(java.util.List.of(sampleAppointment(id)), PageRequest.of(0, 10), 1);
        when(appointmentService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/appointments").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));

        verify(appointmentService).findAll(any());
    }

    @Test
    void confirm_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.confirm(id)).thenReturn(sampleAppointment(id));

        mockMvc.perform(patch("/api/v1/appointments/" + id + "/confirm")).andExpect(status().isOk());

        verify(appointmentService).confirm(eq(id));
    }

    @Test
    void cancel_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CancelAppointmentRequest("Patient requested");
        when(appointmentService.cancel(eq(id), any())).thenReturn(sampleAppointment(id));

        patchJson("/api/v1/appointments/" + id + "/cancel", req).andExpect(status().isOk());

        verify(appointmentService).cancel(eq(id), any());
    }

    @Test
    void cancel_invalidBody_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        patchRawJson("/api/v1/appointments/" + id + "/cancel", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void complete_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.complete(eq(id), isNull())).thenReturn(sampleAppointment(id));

        mockMvc.perform(patch("/api/v1/appointments/" + id + "/complete")).andExpect(status().isOk());

        verify(appointmentService).complete(eq(id), isNull());
    }

    @Test
    void markNoShow_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentService.markNoShow(id)).thenReturn(sampleAppointment(id));

        mockMvc.perform(patch("/api/v1/appointments/" + id + "/no-show")).andExpect(status().isOk());

        verify(appointmentService).markNoShow(eq(id));
    }

    private static AppointmentResponse sampleAppointment(UUID id) {
        return sampleAppointment(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID());
    }

    private static AppointmentResponse sampleAppointment(
            UUID id, UUID patientId, UUID doctorId, UUID officeId, UUID typeId) {
        UUID spId = UUID.randomUUID();
        PatientResponse patient =
                new PatientResponse(patientId, "P", "CC", "1", "p@e.com", null, Gender.MALE, PersonStatus.ACTIVE);
        DoctorResponse doctor = new DoctorResponse(
                doctorId,
                "D",
                "CC",
                "2",
                "d@e.com",
                null,
                Gender.FEMALE,
                "RN",
                spId,
                "Spec",
                PersonStatus.ACTIVE);
        OfficeResponse office = new OfficeResponse(officeId, "OC", "Office", null, OfficeStatus.ACTIVE);
        AppointmentTypeResponse type = new AppointmentTypeResponse(typeId, "Consult", 30);
        LocalDateTime start = LocalDateTime.of(2030, 6, 15, 10, 0);
        return new AppointmentResponse(
                id, patient, doctor, office, type, start, start.plusMinutes(30), AppointmentStatus.SCHEDULED, null, null);
    }
}
