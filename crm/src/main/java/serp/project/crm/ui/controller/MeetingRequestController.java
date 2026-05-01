/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CreateMeetingRequest;
import serp.project.crm.core.domain.enums.MeetingRequestStatus;
import serp.project.crm.core.usecase.MeetingRequestUseCase;
import serp.project.crm.kernel.utils.AuthUtils;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/meeting-requests")
@RequiredArgsConstructor
public class MeetingRequestController {

    private final MeetingRequestUseCase meetingRequestUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<?> createMeetingRequest(@Valid @RequestBody CreateMeetingRequest request) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = meetingRequestUseCase.createMeetingRequest(request, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMeetingRequestById(@PathVariable Long id) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = meetingRequestUseCase.getMeetingRequestById(id, getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getMeetingRequests(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) MeetingRequestStatus status) {
        Optional<ResponseEntity<?>> unauthorized = requireTenantContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        PageRequest pageRequest = PageRequest.builder().page(page).size(size).build();
        var response = meetingRequestUseCase.getMeetingRequests(getCurrentTenantId(), pageRequest, status);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelMeetingRequest(@PathVariable Long id) {
        Optional<ResponseEntity<?>> unauthorized = requireUserContext();
        if (unauthorized.isPresent()) {
            return unauthorized.get();
        }

        var response = meetingRequestUseCase.cancelMeetingRequest(id, getCurrentUserId(), getCurrentTenantId());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    private Optional<ResponseEntity<?>> requireTenantContext() {
        if (authUtils.getCurrentTenantId().isPresent()) {
            return Optional.empty();
        }

        var response = responseUtils.unauthorized("Authentication context is required");
        return Optional.of(ResponseEntity.status(response.getCode()).body(response));
    }

    private Optional<ResponseEntity<?>> requireUserContext() {
        if (authUtils.getCurrentTenantId().isPresent() && authUtils.getCurrentUserId().isPresent()) {
            return Optional.empty();
        }

        var response = responseUtils.unauthorized("Authentication context is required");
        return Optional.of(ResponseEntity.status(response.getCode()).body(response));
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElse(null);
    }

    private Long getCurrentUserId() {
        return authUtils.getCurrentUserId().orElse(null);
    }
}
