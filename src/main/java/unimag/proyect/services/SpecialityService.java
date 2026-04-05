package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateSpecialtyRequest;
import unimag.proyect.api.dto.response.SpecialityResponse;

import java.util.List;
import java.util.UUID;

public interface SpecialityService {

    SpecialityResponse create(CreateSpecialtyRequest request);

    SpecialityResponse findById(UUID id);

    List<SpecialityResponse> findAll();
}