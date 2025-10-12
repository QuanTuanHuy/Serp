/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreatePermissionDto;
import serp.project.account.core.usecase.PermissionUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    private final PermissionUseCase permissionUseCase;

    @PostMapping
    public ResponseEntity<?> createPermission(@Valid @RequestBody CreatePermissionDto request) {
        var response = permissionUseCase.createPermission(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllPermissions() {
        var response = permissionUseCase.getAllPermissions();
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
