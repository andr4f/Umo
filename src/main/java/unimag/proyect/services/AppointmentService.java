package unimag.proyect.services;

import unimag.proyect.api.dto.request.CancelAppointmentRequest;
import unimag.proyect.api.dto.request.CreateAppointmentRequest;
import unimag.proyect.api.dto.response.AppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    /**
     * Crea una cita aplicando las 10 reglas del taller:
     * - Paciente/Doctor/Consultorio existen y están activos
     * - Fecha no es pasada
     * - Dentro del horario laboral del doctor
     * - endTime calculado por el service (no viene del cliente)
     * - Sin traslape de doctor, consultorio ni paciente
     * - Estado inicial: SCHEDULED
     */
    AppointmentResponse create(CreateAppointmentRequest request);

    AppointmentResponse findById(UUID id);

    List<AppointmentResponse> findAll();

    /**
     * SCHEDULED -> CONFIRMED
     * Lanza BusinessException si el estado no es SCHEDULED
     */
    AppointmentResponse confirm(UUID id);

    /**
     * SCHEDULED | CONFIRMED -> CANCELLED
     * Motivo obligatorio (viene en el body)
     * Lanza BusinessException si el estado es COMPLETED o NO_SHOW
     */
    AppointmentResponse cancel(UUID id, CancelAppointmentRequest request);

    /**
     * CONFIRMED -> COMPLETED
     * Solo si la hora actual >= startTime de la cita
     * Permite registrar observaciones opcionales
     */
    AppointmentResponse complete(UUID id, String observations);

    /**
     * CONFIRMED -> NO_SHOW
     * Solo si la hora actual >= startTime de la cita
     */
    AppointmentResponse markNoShow(UUID id);
}