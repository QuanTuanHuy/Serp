/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import serp.project.account.core.domain.dto.request.AddPermissionToRoleDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.dto.request.UpdateRoleDto;
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

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<?> addPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AddPermissionToRoleDto request) {
        var response = roleUseCase.addPermissionsToRole(roleId, request.getPermissionIds());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        var response = roleUseCase.getAllRoles();
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{roleId}")
    public ResponseEntity<?> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleDto request) {
        var response = roleUseCase.updateRole(roleId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
