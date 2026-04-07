package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.request.UpdateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;
import unimag.proyect.entities.Role;
import unimag.proyect.entities.SystemUser;
import unimag.proyect.enums.PersonStatus;
import unimag.proyect.exceptions.ConflictException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.SystemUserMapper;
import unimag.proyect.repositories.RoleRepository;
import unimag.proyect.repositories.SystemUserRepository;
import unimag.proyect.services.SystemUserService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemUserServiceImpl implements SystemUserService {

    private final SystemUserRepository systemUserRepository;
    private final RoleRepository       roleRepository;
    private final SystemUserMapper     systemUserMapper;
    private final PasswordEncoder      passwordEncoder;

    @Override
    public SystemUserResponse create(CreateSystemUserRequest request) {

        if (systemUserRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }
        if (systemUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.roleId()));

        SystemUser user = systemUserMapper.toEntity(request);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(PersonStatus.ACTIVE);

        return systemUserMapper.toResponse(systemUserRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemUserResponse findById(UUID id) {
        SystemUser user = systemUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", id));
        return systemUserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemUserResponse> findAll() {
        return systemUserRepository.findAll().stream()
                .map(systemUserMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemUserResponse findByUsername(String username) {
        SystemUser user = systemUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", username));
        return systemUserMapper.toResponse(user);
    }

    @Override
    public SystemUserResponse update(UUID id, UpdateSystemUserRequest request) {
        SystemUser user = systemUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SystemUser", id));

        // email puede cambiar — verificar que no lo use otro usuario
        if (!user.getEmail().equals(request.email())
                && systemUserRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        // rol puede cambiar — resolverlo
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.roleId()));

        systemUserMapper.updateEntity(request, user);
        user.setRole(role);

        return systemUserMapper.toResponse(systemUserRepository.save(user));
    }
}