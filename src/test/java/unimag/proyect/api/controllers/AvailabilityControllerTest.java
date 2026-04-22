package unimag.proyect.api.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;
import unimag.proyect.services.AvailabilityService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AvailabilityController.class)
class AvailabilityControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void getAvailableSlots_returns200() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2030, 6, 1);
        var slot = new AvailabilitySlotResponse(LocalDateTime.of(2030, 6, 1, 9, 0), LocalDateTime.of(2030, 6, 1, 9, 30));
        when(availabilityService.getAvailableSlots(eq(doctorId), eq(date), eq(typeId))).thenReturn(List.of(slot));

        mockMvc.perform(
                        get("/api/v1/availability/doctors/" + doctorId)
                                .param("date", "2030-06-01")
                                .param("appointmentTypeId", typeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").exists());

        verify(availabilityService).getAvailableSlots(eq(doctorId), eq(date), eq(typeId));
    }
}
