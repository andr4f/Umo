package unimag.proyect.repositories;

import unimag.proyect.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // Query Methods — campos heredados de Person
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByDocumentNumber(String documentNumber);
    List<Patient> findByFullNameContainingIgnoreCase(String name);
    List<Patient> findByStatus(String status);

    // Buscar paciente con todas sus citas cargadas (evita N+1)
    @Query("SELECT p FROM Patient p LEFT JOIN FETCH p.appointments WHERE p.idPerson = :id")
    Optional<Patient> findByIdWithAppointments(@Param("id") UUID id);

    // Pacientes que tienen citas con un doctor específico
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.doctor.idPerson = :doctorId")
    List<Patient> findPatientsByDoctorId(@Param("doctorId") UUID doctorId);

    // Pacientes con citas en un estado específico (SCHEDULED, CANCELLED, etc.)
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.status = :status")
    List<Patient> findPatientsByAppointmentStatus(@Param("status") String status);
}