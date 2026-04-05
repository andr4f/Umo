package unimag.proyect.services;

import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {

    /**
     * Retorna los slots libres y completos de un doctor en una fecha.
     * Lógica:
     *   1. Obtiene el horario laboral del doctor para el DayOfWeek de la fecha
     *   2. Fragmenta en bloques iguales según duración del tipo de cita (parámetro)
     *   3. Resta los bloques ocupados por citas existentes (SCHEDULED/CONFIRMED)
     *   4. Devuelve solo bloques 100% libres
     *
     * GET /api/availability/doctors/{doctorId}?date=YYYY-MM-DD&appointmentTypeId=UUID
     */
    List<AvailabilitySlotResponse> getAvailableSlots(UUID doctorId,
                                                     LocalDate date,
                                                     UUID appointmentTypeId);
}