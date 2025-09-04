/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.usecase.RoleUseCase;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleUseCase roleUseCase;

    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleDto createRoleDto) {
        var response = roleUseCase.createRole(createRoleDto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        var response = roleUseCase.getAllRoles();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/realm")
    public ResponseEntity<?> createRealmRole(@RequestBody CreateRoleDto request) {
        var response = roleUseCase.createRealmRole(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/client")
    public ResponseEntity<?> createClientRole(@Valid @RequestBody CreateClientRoleDto request) {
        var response = roleUseCase.createClientRole(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
