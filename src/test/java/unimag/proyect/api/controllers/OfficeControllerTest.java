package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;
import unimag.proyect.enums.OfficeStatus;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.OfficeService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OfficeController.class)
class OfficeControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private OfficeService officeService;

    @Test
    void create_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CreateOfficeRequest("O1", "Office 1", "Floor 2");
        var res = new OfficeResponse(id, "O1", "Office 1", "Floor 2", OfficeStatus.ACTIVE);
        when(officeService.create(any())).thenReturn(res);

        postJson("/api/v1/offices", req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value("O1"));

        verify(officeService).create(any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        postRawJson("/api/v1/offices", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void list_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(officeService.findAll())
                .thenReturn(List.of(new OfficeResponse(id, "A", "N", null, OfficeStatus.ACTIVE)));

        getJson("/api/v1/offices").andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id.toString()));

        verify(officeService).findAll();
    }

    @Test
    void update_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new UpdateOfficeRequest("New name", "Loc", OfficeStatus.INACTIVE);
        var res = new OfficeResponse(id, "C", "New name", "Loc", OfficeStatus.INACTIVE);
        when(officeService.update(eq(id), any())).thenReturn(res);

        patchJson("/api/v1/offices/" + id, req)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(officeService).update(eq(id), any());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new UpdateOfficeRequest("N", null, OfficeStatus.ACTIVE);
        when(officeService.update(eq(id), any())).thenThrow(new ResourceNotFoundException("Office", id));

        patchJson("/api/v1/offices/" + id, req).andExpect(status().isNotFound());
    }
}
