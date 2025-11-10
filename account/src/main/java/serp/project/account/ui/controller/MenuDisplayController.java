/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.AssignMenuDisplayToRoleDto;
import serp.project.account.core.domain.dto.request.CreateMenuDisplayDto;
import serp.project.account.core.domain.dto.request.GetMenuDisplayParams;
import serp.project.account.core.domain.dto.request.UpdateMenuDisplayDto;
import serp.project.account.core.usecase.MenuDisplayUseCase;
import serp.project.account.kernel.utils.AuthUtils;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MenuDisplayController {
    private final MenuDisplayUseCase menuDisplayUseCase;

    private final AuthUtils authUtils;

    @PostMapping("/menu-displays")
    public ResponseEntity<?> createMenuDisplay(@Valid @RequestBody CreateMenuDisplayDto request) {
        var response = menuDisplayUseCase.createMenuDisplay(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/menu-displays/{id}")
    public ResponseEntity<?> updateMenuDisplay(@PathVariable Long id,
            @Valid @RequestBody UpdateMenuDisplayDto request) {
        var response = menuDisplayUseCase.updateMenuDisplay(id, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/menu-displays/{id}")
    public ResponseEntity<?> deleteMenuDisplay(@PathVariable Long id) {
        var response = menuDisplayUseCase.deleteMenuDisplay(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/menu-displays/get-by-module/{moduleId}")
    public ResponseEntity<?> getMenuDisplaysByModuleId(@PathVariable Long moduleId) {
        var response = menuDisplayUseCase.getMenuDisplaysByModuleId(moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/menu-displays/get-by-module-and-user")
    public ResponseEntity<?> getMenuDisplaysByModuleIdAndUserId(
            @RequestParam Long moduleId) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        var response = menuDisplayUseCase.getMenuDisplaysByModuleIdAndUserId(moduleId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/menu-displays")
    public ResponseEntity<?> getAllMenuDisplays(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Long moduleId) {
        var params = GetMenuDisplayParams.builder()
                .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir)
                .moduleId(moduleId)
                .build();
        var response = menuDisplayUseCase.getAllMenuDisplays(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/menu-displays/assign-to-role")
    public ResponseEntity<?> assignMenuDisplaysToRole(@Valid @RequestBody AssignMenuDisplayToRoleDto request) {
        var response = menuDisplayUseCase.assignMenuDisplaysToRole(request.getRoleId(), request.getMenuDisplayIds());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/menu-displays/unassign-from-role")
    public ResponseEntity<?> unassignMenuDisplaysFromRole(@Valid @RequestBody AssignMenuDisplayToRoleDto request) {
        var response = menuDisplayUseCase.unassignMenuDisplaysFromRole(request.getRoleId(),
                request.getMenuDisplayIds());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/menu-displays/get-by-role-ids")
    public ResponseEntity<?> getMenuDisplaysByRoleIds(@RequestParam List<Long> roleIds) {
        var response = menuDisplayUseCase.getMenuDisplaysByRoleIds(roleIds);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
