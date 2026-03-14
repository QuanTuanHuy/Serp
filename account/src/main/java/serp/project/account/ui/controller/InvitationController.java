/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AcceptInvitationRequest;
import serp.project.account.core.domain.dto.request.InviteUserRequest;
import serp.project.account.core.usecase.InvitationUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class InvitationController {
    private final InvitationUseCase invitationUseCase;
    private final AuthUtils authUtils;

    @PostMapping("/organizations/{organizationId}/invitations")
    public ResponseEntity<?> inviteUser(
            @PathVariable Long organizationId,
            @Valid @RequestBody InviteUserRequest request) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        Long invitedBy = authUtils.getCurrentUserId().orElse(null);
        var response = invitationUseCase.inviteUser(organizationId, request, invitedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/invitations")
    public ResponseEntity<?> getInvitations(
            @PathVariable Long organizationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = invitationUseCase.getInvitations(organizationId, status, page, pageSize);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/organizations/{organizationId}/invitations/{invitationId}")
    public ResponseEntity<?> cancelInvitation(
            @PathVariable Long organizationId,
            @PathVariable Long invitationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = invitationUseCase.cancelInvitation(organizationId, invitationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/organizations/{organizationId}/invitations/{invitationId}/resend")
    public ResponseEntity<?> resendInvitation(
            @PathVariable Long organizationId,
            @PathVariable Long invitationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = invitationUseCase.resendInvitation(organizationId, invitationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    // Public endpoint - no auth required
    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable String token,
            @Valid @RequestBody AcceptInvitationRequest request) {
        var response = invitationUseCase.acceptInvitation(token, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
