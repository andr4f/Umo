package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateOfficeRequest;
import unimag.proyect.api.dto.request.UpdateOfficeRequest;
import unimag.proyect.api.dto.response.OfficeResponse;

import java.util.List;
import java.util.UUID;

public interface OfficeService {

    OfficeResponse create(CreateOfficeRequest request);

    List<OfficeResponse> findAll();

    OfficeResponse update(UUID id, UpdateOfficeRequest request);
}