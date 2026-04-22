package unimag.proyect.api.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.response.RoleResponse;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.RoleService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
class RoleControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private RoleService roleService;

    @Test
    void findAll_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findAll()).thenReturn(List.of(new RoleResponse(id, "ADMIN")));

        getJson("/api/v1/roles").andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("ADMIN"));

        verify(roleService).findAll();
    }

    @Test
    void findById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findById(id)).thenReturn(new RoleResponse(id, "USER"));

        getJson("/api/v1/roles/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("USER"));

        verify(roleService).findById(eq(id));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findById(id)).thenThrow(new ResourceNotFoundException("Role", id));

        getJson("/api/v1/roles/" + id).andExpect(status().isNotFound());
    }
}
