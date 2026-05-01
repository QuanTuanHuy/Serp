/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.ActivityFilterRequest;
import serp.project.crm.core.domain.dto.request.BulkActivityRequest;
import serp.project.crm.core.domain.dto.request.CompleteActivityRequest;
import serp.project.crm.core.domain.dto.request.CreateActivityRequest;
import serp.project.crm.core.domain.dto.request.RescheduleActivityRequest;
import serp.project.crm.core.domain.dto.request.UpdateActivityRequest;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.kernel.utils.AuthUtils;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityUseCase activityUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<?> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.createActivity(request, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivityRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.updateActivity(id, request, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getActivityById(id, getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        var response = activityUseCase.getAllActivities(getCurrentTenantId(), pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchActivities(@Valid @RequestBody ActivityFilterRequest filter) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.filterActivities(filter, getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getActivityStats() {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getActivityStats(getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkOperations(@Valid @RequestBody BulkActivityRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        GeneralResponse<?> response;
        switch (request.getAction()) {
            case COMPLETE:
                response = activityUseCase.bulkCompleteActivities(request, getCurrentUserId(), getCurrentTenantId());
                break;
            case CANCEL:
                response = activityUseCase.bulkCancelActivities(request, getCurrentUserId(), getCurrentTenantId());
                break;
            case DELETE:
                response = activityUseCase.bulkDeleteActivities(request, getCurrentTenantId());
                break;
            case ASSIGN:
                response = activityUseCase.bulkReassignActivities(request, getCurrentUserId(), getCurrentTenantId());
                break;
            default:
                response = responseUtils.badRequest("Invalid bulk action");
        }

        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getActivitiesByType(
            @PathVariable ActivityType type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getActivitiesByType(type, getCurrentTenantId(), buildPageRequest(page, size));
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getActivitiesByStatus(
            @PathVariable ActivityStatus status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getActivitiesByStatus(status, getCurrentTenantId(), buildPageRequest(page, size));
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<?> getActivitiesByAssignee(
            @PathVariable Long assigneeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getActivitiesByAssignee(assigneeId, getCurrentTenantId(), buildPageRequest(page, size));
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueActivities() {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getOverdueActivities(getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingActivities(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.getUpcomingActivities(startDate, endDate, getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeActivity(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CompleteActivityRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.completeActivity(id, request, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelActivity(@PathVariable Long id) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.cancelActivity(id, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleActivity(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleActivityRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.rescheduleActivity(id, request, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = activityUseCase.deleteActivity(id, getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    private Optional<ResponseEntity<?>> requireTenantContext() {
        if (authUtils.getCurrentTenantId().isPresent()) {
            return Optional.empty();
        }
        return Optional.of(toResponseEntity(responseUtils.unauthorized("Tenant context is required")));
    }

    private Optional<ResponseEntity<?>> requireUserContext() {
        if (authUtils.getCurrentTenantId().isPresent() && authUtils.getCurrentUserId().isPresent()) {
            return Optional.empty();
        }
        return Optional.of(toResponseEntity(responseUtils.unauthorized("User context is required")));
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow();
    }

    private Long getCurrentUserId() {
        return authUtils.getCurrentUserId().orElseThrow();
    }

    private PageRequest buildPageRequest(Integer page, Integer size) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    private ResponseEntity<?> toResponseEntity(GeneralResponse<?> response) {
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
