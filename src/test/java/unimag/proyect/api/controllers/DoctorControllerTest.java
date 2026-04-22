package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateDoctorRequest;
import unimag.proyect.api.dto.response.DoctorResponse;
import unimag.proyect.enums.Gender;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.DoctorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class)
class DoctorControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private DoctorService doctorService;

    @Test
    void create_returns201AndLocation() throws Exception {
        UUID id = UUID.randomUUID();
        UUID specialityId = UUID.randomUUID();
        var body = new CreateDoctorRequest(
                "Dr. House",
                "CC",
                "123",
                "house@example.com",
                "555",
                Gender.MALE,
                "REG-1",
                specialityId);
        var response = new DoctorResponse(
                id,
                body.fullName(),
                body.documentType(),
                body.documentNumber(),
                body.email(),
                body.phone(),
                body.gender(),
                body.registerNum(),
                specialityId,
                "Cardiology",
                PersonStatus.ACTIVE);
        when(doctorService.create(any(CreateDoctorRequest.class))).thenReturn(response);

        postJson("/api/v1/doctors", body)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/doctors/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("Dr. House"));

        verify(doctorService).create(any(CreateDoctorRequest.class));
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        postRawJson("/api/v1/doctors", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = sampleDoctor(id);
        when(doctorService.findById(id)).thenReturn(dto);

        getJson("/api/v1/doctors/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registerNum").value("REG-1"));

        verify(doctorService).findById(eq(id));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(doctorService.findById(id)).thenThrow(new ResourceNotFoundException("Doctor", id));

        getJson("/api/v1/doctors/" + id)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void list_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(List.of(sampleDoctor(id)), PageRequest.of(0, 10), 1);
        when(doctorService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/doctors").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));

        verify(doctorService).findAll(any());
    }

    private static DoctorResponse sampleDoctor(UUID id) {
        UUID sp = UUID.randomUUID();
        return new DoctorResponse(
                id, "Dr. X", "CC", "1", "x@example.com", null, Gender.OTHER, "REG-1", sp, "Spec", PersonStatus.ACTIVE);
    }
}
