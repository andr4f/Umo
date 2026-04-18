package unimag.proyect.api.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import unimag.proyect.api.dto.request.CreateSystemUserRequest;
import unimag.proyect.api.dto.response.SystemUserResponse;
import unimag.proyect.services.SystemUserService;

@RestController
@RequestMapping("/api/v1/system-users")
@RequiredArgsConstructor
@Validated

public class SystemUserController {

    private final SystemUserService service;

    @PostMapping
    public ResponseEntity<SystemUserResponse> create(@Valid @RequestBody CreateSystemUserRequest request,
        UriComponentsBuilder uBuilder) {
        var systemUserCreated = service.create(request);
        var location = uBuilder.path("/api/v1/system-users/{id}")
                       .buildAndExpand(systemUserCreated.idPerson())
                       .toUri();

        return ResponseEntity.created(location).body(systemUserCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemUserResponse> getById(@PathVariable UUID id) {
        var systemUser = service.findById(id);
        return ResponseEntity.ok(systemUser);
    }

    @GetMapping
    public ResponseEntity<Page<SystemUserResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size);
        var result = service.findAll(pageable);
        return ResponseEntity.ok(result);
    }

}
