package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.usecase.ModuleUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {
    private final ModuleUseCase moduleUseCase;
    private final UserUseCase userUseCase;

    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createModule(@Valid @RequestBody CreateModuleDto request) {
        var response = moduleUseCase.createModule(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<?> getModuleById(@PathVariable Long moduleId) {
        var response = moduleUseCase.getModuleById(moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<?> updateModule(@PathVariable Long moduleId, @Valid @RequestBody UpdateModuleDto request) {
        var response = moduleUseCase.updateModule(moduleId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{moduleId}/registration")
    public ResponseEntity<?> userRegisterModule(@PathVariable Long moduleId) {
        Long userId = authUtils.getCurrentUserId().isPresent() ? authUtils.getCurrentUserId().get() : null;
        Long tenantId = authUtils.getCurrentTenantId().isPresent() ? authUtils.getCurrentTenantId().get() : null;

        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllModules() {
        var response = moduleUseCase.getAllModules();
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
