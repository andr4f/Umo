package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateAppointmentTypeRequest;
import unimag.proyect.api.dto.response.AppointmentTypeResponse;

import java.util.List;
import java.util.UUID;

public interface AppointmentTypeService {

    AppointmentTypeResponse create(CreateAppointmentTypeRequest request);

    AppointmentTypeResponse findById(UUID id);

    List<AppointmentTypeResponse> findAll();
}