/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityUseCase activityUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }
        var response = activityUseCase.createActivity(request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }
        var response = activityUseCase.updateActivity(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = activityUseCase.getActivityById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();

        var response = activityUseCase.getAllActivities(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeActivity(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = activityUseCase.completeActivity(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelActivity(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = activityUseCase.cancelActivity(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = activityUseCase.deleteActivity(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
