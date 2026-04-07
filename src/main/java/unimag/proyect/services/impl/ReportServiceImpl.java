package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.response.reports.DoctorProductivityResponse;
import unimag.proyect.api.dto.response.reports.NoShowPatientResponse;
import unimag.proyect.api.dto.response.reports.OfficeOccupancyResponse;
import unimag.proyect.entities.Appointment;
import unimag.proyect.entities.Doctor;
import unimag.proyect.entities.Office;
import unimag.proyect.entities.Patient;
import unimag.proyect.enums.AppointmentStatus;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.DoctorRepository;
import unimag.proyect.repositories.OfficeRepository;
import unimag.proyect.services.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int SLOT_MINUTES = 30;
    private static final LocalTime DAY_START = LocalTime.of(8, 0);
    private static final LocalTime DAY_END = LocalTime.of(18, 0);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;

    @Override
    public List<OfficeOccupancyResponse> getOfficeOccupancy(LocalDate start, LocalDate end) {
        List<Office> offices = officeRepository.findAll();

        List<Appointment> relevantAppointments = new ArrayList<>();
        relevantAppointments.addAll(appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED));
        relevantAppointments.addAll(appointmentRepository.findByStatus(AppointmentStatus.CONFIRMED));
        relevantAppointments.addAll(appointmentRepository.findByStatus(AppointmentStatus.COMPLETED));

        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.atTime(23, 59, 59);

        relevantAppointments = relevantAppointments.stream()
                .filter(a -> !a.getStartTime().isBefore(rangeStart)
                        && !a.getStartTime().isAfter(rangeEnd))
                .toList();

        List<OfficeOccupancyResponse> result = new ArrayList<>();

        for (Office office : offices) {
            LocalDate date = start;
            while (!date.isAfter(end)) {
                LocalDate finalDate = date;
                List<Appointment> officeAppointments = relevantAppointments.stream()
                        .filter(a -> a.getOffice().getIdOffice().equals(office.getIdOffice())
                                && a.getStartTime().toLocalDate().equals(finalDate))
                        .toList();

                int totalSlots = (int) (DAY_START.until(DAY_END, java.time.temporal.ChronoUnit.MINUTES) / SLOT_MINUTES);
                int occupiedSlots = officeAppointments.size();
                double occupancyPercentage = totalSlots == 0
                        ? 0.0
                        : (occupiedSlots * 100.0) / totalSlots;

                result.add(new OfficeOccupancyResponse(
                        office.getIdOffice(),
                        office.getCode(),
                        date,
                        totalSlots,
                        occupiedSlots,
                        occupancyPercentage
                ));

                date = date.plusDays(1);
            }
        }

        return result;
    }

    @Override
    public List<DoctorProductivityResponse> getDoctorProductivity(LocalDate start, LocalDate end) {
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.atTime(23, 59, 59);

        List<Appointment> completed =
                appointmentRepository.findByStatus(AppointmentStatus.COMPLETED).stream()
                        .filter(a -> !a.getStartTime().isBefore(rangeStart)
                                && !a.getStartTime().isAfter(rangeEnd))
                        .toList();

        Map<UUID, Long> counts = completed.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getIdPerson(),
                        Collectors.counting()
                ));

        Map<UUID, Doctor> doctorsById = doctorRepository.findAll().stream()
                .collect(Collectors.toMap(Doctor::getIdPerson, d -> d));

        return counts.entrySet().stream()
                .map(e -> {
                    Doctor doctor = doctorsById.get(e.getKey());
                    String name = doctor != null ? doctor.getFullName() : "Unknown";
                    return new DoctorProductivityResponse(
                            e.getKey(),
                            name,
                            e.getValue()
                    );
                })
                .sorted(Comparator.comparingLong(DoctorProductivityResponse::completedAppointments).reversed())
                .toList();
    }

    @Override
    public List<NoShowPatientResponse> getNoShowPatients(LocalDate start, LocalDate end) {
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.atTime(23, 59, 59);

        List<Appointment> noShow =
                appointmentRepository.findByStatus(AppointmentStatus.NO_SHOW).stream()
                        .filter(a -> !a.getStartTime().isBefore(rangeStart)
                                && !a.getStartTime().isAfter(rangeEnd))
                        .toList();

        Map<UUID, Long> counts = noShow.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getPatient().getIdPerson(),
                        Collectors.counting()
                ));

        Map<UUID, Patient> patientsById = noShow.stream()
                .map(Appointment::getPatient)
                .collect(Collectors.toMap(Patient::getIdPerson, p -> p, (p1, p2) -> p1));

        return counts.entrySet().stream()
                .map(e -> {
                    Patient patient = patientsById.get(e.getKey());
                    String name = patient != null ? patient.getFullName() : "Unknown";
                    return new NoShowPatientResponse(
                            e.getKey(),
                            name,
                            e.getValue()
                    );
                })
                .sorted(Comparator.comparingLong(NoShowPatientResponse::noShowCount).reversed())
                .toList();
    }
}