package unimag.proyect.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.proyect.api.dto.response.RoleResponse;
import unimag.proyect.entities.Role;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.RoleMapper;
import unimag.proyect.repositories.RoleRepository;
import unimag.proyect.services.RoleService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse findById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        return roleMapper.toResponse(role);
    }

    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }
}