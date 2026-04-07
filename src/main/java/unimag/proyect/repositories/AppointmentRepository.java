package unimag.proyect.repositories;

import unimag.proyect.entities.Appointment;
import unimag.proyect.enums.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // Query Methods
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByPatient_IdPerson(UUID patientId);
    List<Appointment> findByDoctor_IdPerson(UUID doctorId);
    List<Appointment> findByOffice_IdOffice(UUID officeId);

    // Cita con todas sus relaciones cargadas (evita N+1)
    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.patient
            JOIN FETCH a.doctor
            JOIN FETCH a.office
            JOIN FETCH a.appointmentType
            WHERE a.idAppointment = :id
            """)
    Optional<Appointment> findByIdWithDetails(@Param("id") UUID id);

// En AppointmentRepository
        @Query("SELECT a FROM Appointment a " +
                "LEFT JOIN FETCH a.patient " +
                "LEFT JOIN FETCH a.doctor " +
                "LEFT JOIN FETCH a.office " +
                "LEFT JOIN FETCH a.appointmentType")
                List<Appointment> findAllWithDetails();

            // En AppointmentRepository
        @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
        "WHERE a.patient.idPerson = :patientId " +
        "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
        "AND a.startTime < :end AND a.endTime > :start")
        boolean existsPatientConflict(@Param("patientId") UUID patientId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
        // En AppointmentRepository
        @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
        "WHERE a.office.idOffice = :officeId " +
        "AND a.status IN ('SCHEDULED', 'CONFIRMED')")
        boolean existsActiveAppointmentsByOffice(@Param("officeId") UUID officeId);
        // Citas de un doctor en un rango de fechas
        
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.idPerson = :doctorId
            AND a.startTime BETWEEN :start AND :end
            ORDER BY a.startTime ASC
            """)
    List<Appointment> findByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Verificar si un doctor tiene cita en ese horario (choque de citas)
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.doctor.idPerson = :doctorId
            AND a.status NOT IN (unimag.proyect.enums.AppointmentStatus.CANCELLED,
                                unimag.proyect.enums.AppointmentStatus.NO_SHOW)
            AND a.startTime < :endTime
            AND a.endTime > :startTime
            """)
    boolean existsDoctorConflict(
            @Param("doctorId") UUID doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Verificar si un consultorio está ocupado en ese horario
    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.office.idOffice = :officeId
            AND a.status NOT IN (unimag.proyect.enums.AppointmentStatus.CANCELLED,
                                unimag.proyect.enums.AppointmentStatus.NO_SHOW)
            AND a.startTime < :endTime
            AND a.endTime > :startTime
            """)
    boolean existsOfficeConflict(
            @Param("officeId") UUID officeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Historial de citas de un paciente ordenado por fecha
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.patient.idPerson = :patientId
            ORDER BY a.startTime DESC
            """)
    List<Appointment> findPatientHistory(@Param("patientId") UUID patientId);

    
}