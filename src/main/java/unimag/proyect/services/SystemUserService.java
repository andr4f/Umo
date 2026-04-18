package unimag.proyect.services;

import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.request.UpdateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SystemUserService {
    SystemUserResponse create(CreateSystemUserRequest request);
    SystemUserResponse findById(UUID id);
    Page<SystemUserResponse> findAll(Pageable pageable);
    SystemUserResponse findByUsername(String username);
    SystemUserResponse update(UUID id, UpdateSystemUserRequest request);
}