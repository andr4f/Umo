package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.AppointmentTypeService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppointmentTypeController.class)
class AppointmentTypeControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private AppointmentTypeService appointmentTypeService;

    @Test
    void create_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CreateAppointmentTypeRequest("Follow-up", 30);
        var res = new AppointmentTypeResponse(id, "Follow-up", 30);
        when(appointmentTypeService.create(any())).thenReturn(res);

        postJson("/api/v1/appointment-types", req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.durationMinutes").value(30));

        verify(appointmentTypeService).create(any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        postRawJson("/api/v1/appointment-types", "{\"name\":\"\"}").andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentTypeService.findById(id)).thenReturn(new AppointmentTypeResponse(id, "T", 15));

        getJson("/api/v1/appointment-types/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("T"));

        verify(appointmentTypeService).findById(eq(id));
    }

    @Test
    void list_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentTypeService.findAll()).thenReturn(List.of(new AppointmentTypeResponse(id, "A", 20)));

        getJson("/api/v1/appointment-types").andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("A"));

        verify(appointmentTypeService).findAll();
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(appointmentTypeService.findById(id)).thenThrow(new ResourceNotFoundException("Type", id));

        getJson("/api/v1/appointment-types/" + id).andExpect(status().isNotFound());
    }
}
