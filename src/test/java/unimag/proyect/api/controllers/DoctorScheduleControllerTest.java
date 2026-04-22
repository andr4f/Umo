package unimag.proyect.api.controllers;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.request.CreateDoctorScheduleRequest;
import unimag.proyect.api.dto.response.DoctorScheduleResponse;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;
import unimag.proyect.services.DoctorScheduleService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorScheduleController.class)
class DoctorScheduleControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private DoctorScheduleService doctorScheduleService;

    @Test
    void create_returns201() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        var req = new CreateDoctorScheduleRequest(doctorId, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        var res = new DoctorScheduleResponse(
                scheduleId, doctorId, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0), ScheduleStatus.AVAILABLE);
        when(doctorScheduleService.create(eq(doctorId), any())).thenReturn(res);

        postJson("/api/v1/doctors/" + doctorId + "/schedules", req)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.weekDay").value("MONDAY"));

        verify(doctorScheduleService).create(eq(doctorId), any());
    }

    @Test
    void create_invalid_returns400() throws Exception {
        UUID doctorId = UUID.randomUUID();
        postRawJson("/api/v1/doctors/" + doctorId + "/schedules", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void list_returns200() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        var row = new DoctorScheduleResponse(
                scheduleId, doctorId, WeekDay.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), ScheduleStatus.AVAILABLE);
        when(doctorScheduleService.findByDoctor(doctorId)).thenReturn(List.of(row));

        getJson("/api/v1/doctors/" + doctorId + "/schedules")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(scheduleId.toString()));

        verify(doctorScheduleService).findByDoctor(eq(doctorId));
    }
}
