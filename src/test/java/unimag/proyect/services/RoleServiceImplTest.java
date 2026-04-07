package unimag.proyect.services;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.response.RoleResponse;
import unimag.proyect.entities.Role;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.mappers.RoleMapper;
import unimag.proyect.repositories.RoleRepository;
import unimag.proyect.services.impl.RoleServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenRoleExists() {
        UUID id = UUID.randomUUID();
        Role role = new Role(id, "ADMIN");
        RoleResponse expected = new RoleResponse(id, "ADMIN");

        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(expected);

        RoleResponse result = roleService.findById(id);

        assertThat(result).isEqualTo(expected);
        verify(roleRepository).findById(id);
        verify(roleMapper).toResponse(role);
    }

    @Test
    void findById_shouldThrow_whenRoleNotFound() {
        UUID id = UUID.randomUUID();

        when(roleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roleRepository).findById(id);
        verifyNoInteractions(roleMapper);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnMappedResponses() {
        Role admin      = new Role(UUID.randomUUID(), "ADMIN");
        Role doctor     = new Role(UUID.randomUUID(), "DOCTOR");
        Role receptionist = new Role(UUID.randomUUID(), "RECEPTIONIST");

        RoleResponse r1 = new RoleResponse(admin.getId(),         "ADMIN");
        RoleResponse r2 = new RoleResponse(doctor.getId(),        "DOCTOR");
        RoleResponse r3 = new RoleResponse(receptionist.getId(),  "RECEPTIONIST");

        when(roleRepository.findAll()).thenReturn(List.of(admin, doctor, receptionist));
        when(roleMapper.toResponse(admin)).thenReturn(r1);
        when(roleMapper.toResponse(doctor)).thenReturn(r2);
        when(roleMapper.toResponse(receptionist)).thenReturn(r3);

        List<RoleResponse> result = roleService.findAll();

        assertThat(result).hasSize(3)
                .containsExactly(r1, r2, r3);
        verify(roleRepository).findAll();
        verify(roleMapper, times(3)).toResponse(any(Role.class));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoRoles() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<RoleResponse> result = roleService.findAll();

        assertThat(result).isEmpty();
        verify(roleRepository).findAll();
        verifyNoInteractions(roleMapper);
    }
}