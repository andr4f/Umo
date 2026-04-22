package unimag.proyect.api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import unimag.proyect.api.controllers.support.ControllerMvcSliceTest;
import unimag.proyect.api.dto.response.reports.DoctorProductivityResponse;
import unimag.proyect.api.dto.response.reports.NoShowPatientResponse;
import unimag.proyect.api.dto.response.reports.OfficeOccupancyResponse;
import unimag.proyect.services.ReportService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
class ReportControllerTest extends ControllerMvcSliceTest {

    @MockitoBean
    private ReportService reportService;

    @Test
    void officeOccupancy_returns200() throws Exception {
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate end = LocalDate.of(2030, 1, 31);
        var row = new OfficeOccupancyResponse(UUID.randomUUID(), "O1", start, 10, 5, 50.0);
        when(reportService.getOfficeOccupancy(eq(start), eq(end))).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/reports/office-occupancy")
                        .param("start", "2030-01-01")
                        .param("end", "2030-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officeCode").value("O1"));

        verify(reportService).getOfficeOccupancy(eq(start), eq(end));
    }

    @Test
    void doctorProductivity_returns200() throws Exception {
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate end = LocalDate.of(2030, 1, 7);
        var row = new DoctorProductivityResponse(UUID.randomUUID(), "Dr. A", 3L);
        when(reportService.getDoctorProductivity(eq(start), eq(end))).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/reports/doctor-productivity")
                        .param("start", "2030-01-01")
                        .param("end", "2030-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completedAppointments").value(3));

        verify(reportService).getDoctorProductivity(eq(start), eq(end));
    }

    @Test
    void noShowPatients_returns200() throws Exception {
        LocalDate start = LocalDate.of(2030, 2, 1);
        LocalDate end = LocalDate.of(2030, 2, 28);
        var row = new NoShowPatientResponse(UUID.randomUUID(), "Patient", 2L);
        when(reportService.getNoShowPatients(eq(start), eq(end))).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/reports/no-show-patients")
                        .param("start", "2030-02-01")
                        .param("end", "2030-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].noShowCount").value(2));

        verify(reportService).getNoShowPatients(eq(start), eq(end));
    }
}
