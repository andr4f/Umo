package unimag.proyect.services;

import unimag.proyect.api.dto.response.reports.DoctorProductivityResponse;
import unimag.proyect.api.dto.response.reports.NoShowPatientResponse;
import unimag.proyect.api.dto.response.reports.OfficeOccupancyResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    /**
     * Ocupación de cada consultorio entre startDate y endDate.
     * Calcula: totalSlots, occupiedSlots, occupancyPercentage
     * GET /api/reports/office-occupancy?start=YYYY-MM-DD&end=YYYY-MM-DD
     */
    List<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate start, LocalDate end);

    /**
     * Ranking de doctores por cantidad de citas COMPLETED.
     * GET /api/reports/doctor-productivity?start=YYYY-MM-DD&end=YYYY-MM-DD
     */
    List<DoctorProductivityResponse> getDoctorProductivity(LocalDate start, LocalDate end);

    /**
     * Pacientes con mayor cantidad de NO_SHOW en el período.
     * GET /api/reports/no-show-patients?start=YYYY-MM-DD&end=YYYY-MM-DD
     */
    List<NoShowPatientResponse> getNoShowPatients(LocalDate start, LocalDate end);
}