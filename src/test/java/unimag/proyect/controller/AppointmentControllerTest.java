package unimag.proyect.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import unimag.proyect.api.controllers.AppointmentController;
import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean AppointmentService service;

    // ─── datos reutilizables ──────────────────────────────────────────────────
    private final UUID id              = UUID.randomUUID();
    private final UUID patientId       = UUID.randomUUID();
    private final UUID doctorId        = UUID.randomUUID();
    private final UUID officeId        = UUID.randomUUID();
    private final UUID appointmentTypeId = UUID.randomUUID();
    private final LocalDateTime startTime = LocalDateTime.now().plusDays(1);

    // response completo reutilizable
    private AppointmentResponse resp() {
        return new AppointmentResponse(
                id, null, null, null, null,
                startTime, startTime.plusMinutes(30),
                AppointmentStatus.SCHEDULED, null, null
        );
    }

    // ─── POST /api/v1/appointments ────────────────────────────────────────────

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        // ARRANGE
        var req = new CreateAppointmentRequest(patientId, doctorId, officeId, appointmentTypeId, startTime);
        when(service.create(any())).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/appointments/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void create_shouldReturn400WhenBodyInvalid() throws Exception {
        // ARRANGE — body vacío, todos los @NotNull fallan
        // ACT + ASSERT
        mvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/v1/appointments/{id} ───────────────────────────────────────

    @Test
    void getById_shouldReturn200() throws Exception {
        // ARRANGE
        when(service.findById(id)).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(get("/api/v1/appointments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        // ARRANGE
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Appointment", id));

        // ACT + ASSERT
        mvc.perform(get("/api/v1/appointments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ─── GET /api/v1/appointments ────────────────────────────────────────────

    @Test
    void list_shouldReturn200WithPage() throws Exception {
        // ARRANGE
        var page = new PageImpl<>(List.of(resp()), PageRequest.of(0, 10), 1);
        when(service.findAll(any())).thenReturn(page);

        // ACT + ASSERT
        mvc.perform(get("/api/v1/appointments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void list_shouldReturn200WithEmptyPage() throws Exception {
        // ARRANGE
        when(service.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        // ACT + ASSERT
        mvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // ─── PATCH /api/v1/appointments/{id}/confirm ─────────────────────────────

    @Test
    void confirm_shouldReturn200() throws Exception {
        // ARRANGE
        when(service.confirm(id)).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void confirm_shouldReturn404WhenNotFound() throws Exception {
        // ARRANGE
        when(service.confirm(id)).thenThrow(new ResourceNotFoundException("Appointment", id));

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/confirm", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ─── PATCH /api/v1/appointments/{id}/cancel ──────────────────────────────

    @Test
    void cancel_shouldReturn200() throws Exception {
        // ARRANGE
        var req = new CancelAppointmentRequest("Patient requested cancellation");
        when(service.cancel(eq(id), any())).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/cancel", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void cancel_shouldReturn400WhenBodyInvalid() throws Exception {
        // ARRANGE — cancelReason vacío, @NotBlank falla
        var badReq = new CancelAppointmentRequest("");

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/cancel", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(badReq)))
                .andExpect(status().isBadRequest());
    }

    // ─── PATCH /api/v1/appointments/{id}/complete ────────────────────────────

    @Test
    void complete_shouldReturn200WithObservations() throws Exception {
        // ARRANGE
        when(service.complete(eq(id), eq("Everything went well"))).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/complete", id)
                        .param("observations", "Everything went well"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void complete_shouldReturn200WithoutObservations() throws Exception {
        // ARRANGE — observations es @RequestParam(required = false)
        when(service.complete(eq(id), eq(null))).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    // ─── PATCH /api/v1/appointments/{id}/no-show ─────────────────────────────

    @Test
    void markNoShow_shouldReturn200() throws Exception {
        // ARRANGE
        when(service.markNoShow(id)).thenReturn(resp());

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/no-show", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void markNoShow_shouldReturn404WhenNotFound() throws Exception {
        // ARRANGE
        when(service.markNoShow(id)).thenThrow(new ResourceNotFoundException("Appointment", id));

        // ACT + ASSERT
        mvc.perform(patch("/api/v1/appointments/{id}/no-show", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}