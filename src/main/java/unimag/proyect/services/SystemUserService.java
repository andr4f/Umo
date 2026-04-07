package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.request.UpdateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;

import java.util.List;
import java.util.UUID;

public interface SystemUserService {
    SystemUserResponse create(CreateSystemUserRequest request);
    SystemUserResponse findById(UUID id);
    List<SystemUserResponse> findAll();
    SystemUserResponse findByUsername(String username);
    SystemUserResponse update(UUID id, UpdateSystemUserRequest request);
}