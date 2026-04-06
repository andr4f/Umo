package unimag.proyect.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.response.reports.DoctorProductivityResponse;
import unimag.proyect.api.dto.response.reports.NoShowPatientResponse;
import unimag.proyect.api.dto.response.reports.OfficeOccupancyResponse;
import unimag.proyect.entities.*;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.OfficeRepository;
import unimag.proyect.services.impl.ReportServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DoctorRepository      doctorRepository;
    @Mock private OfficeRepository      officeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    // ── entidades base reutilizables ─────────────────────────────────────────

    private UUID officeId;
    private UUID doctorId;
    private UUID patientId;

    private Office  office;
    private Doctor  doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        officeId  = UUID.randomUUID();
        doctorId  = UUID.randomUUID();
        patientId = UUID.randomUUID();

        office = new Office();
        office.setIdOffice(officeId);
        office.setCode("C-101");

        doctor = new Doctor();
        doctor.setIdPerson(doctorId);
        doctor.setFullName("Dr. García");

        patient = new Patient();
        patient.setIdPerson(patientId);
        patient.setFullName("Juan Pérez");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Appointment buildAppointment(AppointmentStatus status,
                                         LocalDateTime startTime,
                                         Office o, Doctor d, Patient p) {
        Appointment a = new Appointment();
        a.setStatus(status);
        a.setStartTime(startTime);
        a.setOffice(o);
        a.setDoctor(d);
        a.setPatient(p);
        return a;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getOfficeOccupancy
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void getOfficeOccupancy_shouldCalculateCorrectly_forSingleDayWithTwoAppointments() {
        LocalDate day = LocalDate.of(2026, 4, 7);

        Appointment a1 = buildAppointment(AppointmentStatus.SCHEDULED,
                day.atTime(9, 0), office, doctor, patient);
        Appointment a2 = buildAppointment(AppointmentStatus.CONFIRMED,
                day.atTime(10, 0), office, doctor, patient);

        when(officeRepository.findAll()).thenReturn(List.of(office));
        when(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED))
                .thenReturn(List.of(a1));
        when(appointmentRepository.findByStatus(AppointmentStatus.CONFIRMED))
                .thenReturn(List.of(a2));
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of());

        List<OfficeOccupancyResponse> result =
                reportService.getOfficeOccupancy(day, day);

        assertThat(result).hasSize(1);
        OfficeOccupancyResponse r = result.get(0);
        assertThat(r.officeId()).isEqualTo(officeId);
        assertThat(r.officeCode()).isEqualTo("C-101");
        assertThat(r.date()).isEqualTo(day);
        assertThat(r.totalSlots()).isEqualTo(20);       // (18-8)h / 30min
        assertThat(r.occupiedSlots()).isEqualTo(2);
        assertThat(r.occupancyPercentage()).isEqualTo(10.0); // 2/20 * 100
    }

    @Test
    void getOfficeOccupancy_shouldReturnZeroOccupancy_whenNoAppointmentsInRange() {
        LocalDate day = LocalDate.of(2026, 4, 7);

        // cita fuera del rango
        Appointment outside = buildAppointment(AppointmentStatus.SCHEDULED,
                LocalDateTime.of(2026, 3, 1, 9, 0), office, doctor, patient);

        when(officeRepository.findAll()).thenReturn(List.of(office));
        when(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED))
                .thenReturn(List.of(outside));
        when(appointmentRepository.findByStatus(AppointmentStatus.CONFIRMED))
                .thenReturn(List.of());
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of());

        List<OfficeOccupancyResponse> result =
                reportService.getOfficeOccupancy(day, day);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).occupiedSlots()).isZero();
        assertThat(result.get(0).occupancyPercentage()).isEqualTo(0.0);
    }

    @Test
    void getOfficeOccupancy_shouldGenerateOneRowPerDayPerOffice() {
        LocalDate start = LocalDate.of(2026, 4, 7);
        LocalDate end   = LocalDate.of(2026, 4, 9);   // 3 días

        Office office2 = new Office();
        office2.setIdOffice(UUID.randomUUID());
        office2.setCode("C-202");

        when(officeRepository.findAll()).thenReturn(List.of(office, office2));
        when(appointmentRepository.findByStatus(any())).thenReturn(List.of());

        List<OfficeOccupancyResponse> result =
                reportService.getOfficeOccupancy(start, end);

        // 2 consultorios × 3 días = 6 filas
        assertThat(result).hasSize(6);
    }

    @Test
    void getOfficeOccupancy_shouldIgnoreCancelledAndNoShowAppointments() {
        LocalDate day = LocalDate.of(2026, 4, 7);

        // El service solo llama findByStatus para SCHEDULED, CONFIRMED, COMPLETED
        // — nunca pide CANCELLED ni NO_SHOW
        when(officeRepository.findAll()).thenReturn(List.of(office));
        when(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED))
                .thenReturn(List.of());
        when(appointmentRepository.findByStatus(AppointmentStatus.CONFIRMED))
                .thenReturn(List.of());
        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of());

        reportService.getOfficeOccupancy(day, day);

        verify(appointmentRepository, never())
                .findByStatus(AppointmentStatus.CANCELLED);
        verify(appointmentRepository, never())
                .findByStatus(AppointmentStatus.NO_SHOW);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getDoctorProductivity
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void getDoctorProductivity_shouldReturnDescendingOrder() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        UUID doctorId2 = UUID.randomUUID();
        Doctor doctor2 = new Doctor();
        doctor2.setIdPerson(doctorId2);
        doctor2.setFullName("Dra. López");

        // doctor tiene 1 cita, doctor2 tiene 2 citas
        Appointment c1 = buildAppointment(AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 5, 9, 0), office, doctor, patient);
        Appointment c2 = buildAppointment(AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 6, 9, 0), office, doctor2, patient);
        Appointment c3 = buildAppointment(AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 4, 7, 9, 0), office, doctor2, patient);

        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of(c1, c2, c3));
        when(doctorRepository.findAll()).thenReturn(List.of(doctor, doctor2));

        List<DoctorProductivityResponse> result =
                reportService.getDoctorProductivity(start, end);

        assertThat(result).hasSize(2);
        // doctor2 primero por tener 2 citas
        assertThat(result.get(0).doctorId()).isEqualTo(doctorId2);
        assertThat(result.get(0).completedAppointments()).isEqualTo(2);
        assertThat(result.get(1).doctorId()).isEqualTo(doctorId);
        assertThat(result.get(1).completedAppointments()).isEqualTo(1);
    }

    @Test
    void getDoctorProductivity_shouldExcludeAppointmentsOutsideRange() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        // cita fuera del rango (mayo)
        Appointment outside = buildAppointment(AppointmentStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 1, 9, 0), office, doctor, patient);

        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of(outside));
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        List<DoctorProductivityResponse> result =
                reportService.getDoctorProductivity(start, end);

        assertThat(result).isEmpty();
    }

    @Test
    void getDoctorProductivity_shouldReturnEmptyList_whenNoCompletedAppointments() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        when(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED))
                .thenReturn(List.of());
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        List<DoctorProductivityResponse> result =
                reportService.getDoctorProductivity(start, end);

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getNoShowPatients
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    void getNoShowPatients_shouldReturnDescendingOrder() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        UUID patient2Id = UUID.randomUUID();
        Patient patient2 = new Patient();
        patient2.setIdPerson(patient2Id);
        patient2.setFullName("María Torres");

        // patient 1 no-show, patient2 2 no-shows
        Appointment ns1 = buildAppointment(AppointmentStatus.NO_SHOW,
                LocalDateTime.of(2026, 4, 5, 9, 0), office, doctor, patient);
        Appointment ns2 = buildAppointment(AppointmentStatus.NO_SHOW,
                LocalDateTime.of(2026, 4, 6, 9, 0), office, doctor, patient2);
        Appointment ns3 = buildAppointment(AppointmentStatus.NO_SHOW,
                LocalDateTime.of(2026, 4, 7, 9, 0), office, doctor, patient2);

        when(appointmentRepository.findByStatus(AppointmentStatus.NO_SHOW))
                .thenReturn(List.of(ns1, ns2, ns3));

        List<NoShowPatientResponse> result =
                reportService.getNoShowPatients(start, end);

        assertThat(result).hasSize(2);
        // patient2 primero por tener 2 no-shows
        assertThat(result.get(0).patientId()).isEqualTo(patient2Id);
        assertThat(result.get(0).noShowCount()).isEqualTo(2);
        assertThat(result.get(1).patientId()).isEqualTo(patientId);
        assertThat(result.get(1).noShowCount()).isEqualTo(1);
    }

    @Test
    void getNoShowPatients_shouldExcludeAppointmentsOutsideRange() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        Appointment outside = buildAppointment(AppointmentStatus.NO_SHOW,
                LocalDateTime.of(2026, 3, 1, 9, 0), office, doctor, patient);

        when(appointmentRepository.findByStatus(AppointmentStatus.NO_SHOW))
                .thenReturn(List.of(outside));

        List<NoShowPatientResponse> result =
                reportService.getNoShowPatients(start, end);

        assertThat(result).isEmpty();
    }

    @Test
    void getNoShowPatients_shouldReturnEmptyList_whenNoNoShows() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end   = LocalDate.of(2026, 4, 30);

        when(appointmentRepository.findByStatus(AppointmentStatus.NO_SHOW))
                .thenReturn(List.of());

        List<NoShowPatientResponse> result =
                reportService.getNoShowPatients(start, end);

        assertThat(result).isEmpty();
    }
}