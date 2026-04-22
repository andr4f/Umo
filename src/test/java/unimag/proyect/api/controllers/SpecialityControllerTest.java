package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.SpecialityService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpecialityController.class)
class SpecialityControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private SpecialityService specialityService;

    @Test
    void create_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CreateSpecialtyRequest("Cardiology");
        var res = new SpecialityResponse(id, "Cardiology");
        when(specialityService.create(any())).thenReturn(res);

        postJson("/api/v1/specialities", req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Cardiology"));

        verify(specialityService).create(any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        postRawJson("/api/v1/specialities", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(specialityService.findById(id)).thenReturn(new SpecialityResponse(id, "X"));

        getJson("/api/v1/specialities/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.toString()));

        verify(specialityService).findById(eq(id));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(specialityService.findById(id)).thenThrow(new ResourceNotFoundException("Speciality", id));

        getJson("/api/v1/specialities/" + id).andExpect(status().isNotFound());
    }

    @Test
    void list_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(specialityService.findAll()).thenReturn(List.of(new SpecialityResponse(id, "A")));

        getJson("/api/v1/specialities").andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("A"));

        verify(specialityService).findAll();
    }
}
