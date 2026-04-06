package unimag.proyect.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import unimag.proyect.services.impl.SystemUserServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceImplTest {

    @Mock private SystemUserRepository systemUserRepository;
    @Mock private RoleRepository       roleRepository;
    @Mock private SystemUserMapper     systemUserMapper;
    @Mock private PasswordEncoder      passwordEncoder;

    @InjectMocks
    private SystemUserServiceImpl systemUserService;

    private UUID userId;
    private UUID roleId;
    private Role role;
    private SystemUser user;
    private SystemUserResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role(roleId, "ADMIN");

        user = new SystemUser();
        user.setIdPerson(userId);
        user.setUsername("jdoe");
        user.setEmail("jdoe@unimag.edu");
        user.setRole(role);
        user.setStatus(PersonStatus.ACTIVE);

        response = new SystemUserResponse(userId, "John Doe", "jdoe", "ADMIN");
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_shouldSaveUser_whenValid() {
        CreateSystemUserRequest request = new CreateSystemUserRequest(
                "John Doe", "CC", "123456", "jdoe@unimag.edu",
                "jdoe", "secret123", roleId
        );

        when(systemUserRepository.existsByUsername("jdoe")).thenReturn(false);
        when(systemUserRepository.existsByEmail("jdoe@unimag.edu")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(systemUserMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(systemUserRepository.save(user)).thenReturn(user);
        when(systemUserMapper.toResponse(user)).thenReturn(response);

        SystemUserResponse result = systemUserService.create(request);

        assertThat(result).isEqualTo(response);
        verify(passwordEncoder).encode("secret123");
        verify(systemUserRepository).save(user);
    }

    @Test
    void create_shouldThrow_whenUsernameDuplicated() {
        CreateSystemUserRequest request = new CreateSystemUserRequest(
                "John Doe", "CC", "123456", "jdoe@unimag.edu",
                "jdoe", "secret123", roleId
        );

        when(systemUserRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> systemUserService.create(request))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(roleRepository, systemUserMapper, passwordEncoder);
    }

    @Test
    void create_shouldThrow_whenEmailDuplicated() {
        CreateSystemUserRequest request = new CreateSystemUserRequest(
                "John Doe", "CC", "123456", "jdoe@unimag.edu",
                "jdoe", "secret123", roleId
        );

        when(systemUserRepository.existsByUsername("jdoe")).thenReturn(false);
        when(systemUserRepository.existsByEmail("jdoe@unimag.edu")).thenReturn(true);

        assertThatThrownBy(() -> systemUserService.create(request))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(roleRepository, systemUserMapper, passwordEncoder);
    }

    @Test
    void create_shouldThrow_whenRoleNotFound() {
        CreateSystemUserRequest request = new CreateSystemUserRequest(
                "John Doe", "CC", "123456", "jdoe@unimag.edu",
                "jdoe", "secret123", roleId
        );

        when(systemUserRepository.existsByUsername("jdoe")).thenReturn(false);
        when(systemUserRepository.existsByEmail("jdoe@unimag.edu")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemUserService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(systemUserMapper, passwordEncoder);
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnResponse_whenExists() {
        when(systemUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(systemUserMapper.toResponse(user)).thenReturn(response);

        assertThat(systemUserService.findById(userId)).isEqualTo(response);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(systemUserRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemUserService.findById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(systemUserMapper);
    }

    // ─── findByUsername ───────────────────────────────────────────────────────

    @Test
    void findByUsername_shouldReturnResponse_whenExists() {
        when(systemUserRepository.findByUsername("jdoe")).thenReturn(Optional.of(user));
        when(systemUserMapper.toResponse(user)).thenReturn(response);

        assertThat(systemUserService.findByUsername("jdoe")).isEqualTo(response);
    }

    @Test
    void findByUsername_shouldThrow_whenNotFound() {
        when(systemUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemUserService.findByUsername("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(systemUserMapper);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnMappedList() {
        when(systemUserRepository.findAll()).thenReturn(List.of(user));
        when(systemUserMapper.toResponse(user)).thenReturn(response);

        assertThat(systemUserService.findAll()).containsExactly(response);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldSave_whenValid() {
        UpdateSystemUserRequest request =
                new UpdateSystemUserRequest("John Updated", "jdoe@unimag.edu", roleId);

        when(systemUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(systemUserRepository.save(user)).thenReturn(user);
        when(systemUserMapper.toResponse(user)).thenReturn(response);

        SystemUserResponse result = systemUserService.update(userId, request);

        assertThat(result).isEqualTo(response);
        verify(systemUserMapper).updateEntity(request, user);
        verify(systemUserRepository).save(user);
    }

    @Test
    void update_shouldThrow_whenEmailTakenByAnotherUser() {
        UpdateSystemUserRequest request =
                new UpdateSystemUserRequest("John Updated", "other@unimag.edu", roleId);

        when(systemUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(systemUserRepository.existsByEmail("other@unimag.edu")).thenReturn(true);

        assertThatThrownBy(() -> systemUserService.update(userId, request))
                .isInstanceOf(ConflictException.class);

        verify(systemUserMapper, never()).updateEntity(any(), any());
        verifyNoInteractions(roleRepository);
    }

    @Test
    void update_shouldThrow_whenNewRoleNotFound() {
        UUID newRoleId = UUID.randomUUID();
        UpdateSystemUserRequest request =
                new UpdateSystemUserRequest("John Updated", "jdoe@unimag.edu", newRoleId);

        when(systemUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(newRoleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemUserService.update(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(systemUserMapper, never()).updateEntity(any(), any());
    }
}