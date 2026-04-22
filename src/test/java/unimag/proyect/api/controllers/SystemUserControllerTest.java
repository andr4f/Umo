package unimag.proyect.api.controllers;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.services.SystemUserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SystemUserController.class)
class SystemUserControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private SystemUserService systemUserService;

    @Test
    void create_returns201() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        var req = new CreateSystemUserRequest(
                "John Doe", "CC", "123456", "jdoe@unimag.edu", "jdoe", "secret12345", roleId);
        var res = new SystemUserResponse(personId, "John Doe", "jdoe", "ADMIN");
        when(systemUserService.create(any())).thenReturn(res);

        postJson("/api/v1/system-users", req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("jdoe"));

        verify(systemUserService).create(any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        postRawJson("/api/v1/system-users", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new SystemUserResponse(id, "U", "u", "ROLE");
        when(systemUserService.findById(id)).thenReturn(res);

        getJson("/api/v1/system-users/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.roleName").value("ROLE"));

        verify(systemUserService).findById(eq(id));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(systemUserService.findById(id)).thenThrow(new ResourceNotFoundException("User", id));

        getJson("/api/v1/system-users/" + id).andExpect(status().isNotFound());
    }

    @Test
    void list_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        var row = new SystemUserResponse(id, "A", "a", "R");
        when(systemUserService.findAll(any()))
                .thenReturn(new PageImpl<>(java.util.List.of(row), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/system-users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("a"));

        verify(systemUserService).findAll(any());
    }
}
