/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.dto.request.AcceptInvitationRequest;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.InviteUserRequest;
import serp.project.account.core.domain.entity.UserInvitationEntity;
import serp.project.account.core.domain.enums.UserType;
import serp.project.account.core.exception.AppException;
import serp.project.account.kernel.property.PasswordResetProperties;
import serp.project.account.core.port.store.IUserInvitationPort;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.core.service.INotificationService;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationUseCase {
    private final IUserInvitationPort invitationPort;
    private final IUserPort userPort;
    private final IOrganizationService organizationService;
    private final INotificationService notificationService;
    private final UserUseCase userUseCase;

    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;
    private final PasswordResetProperties passwordResetProperties;

    private static final int INVITATION_EXPIRY_DAYS = 7;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> inviteUser(Long organizationId, InviteUserRequest request, Long invitedBy) {
        try {
            var existingUser = userPort.getUserByEmail(request.getEmail());
            if (existingUser != null && existingUser.getPrimaryOrganizationId().equals(organizationId)) {
                return responseUtils.badRequest(Constants.ErrorMessage.USER_ALREADY_EXISTS);
            }

            var existingInvitation = invitationPort.getPendingByOrgAndEmail(organizationId, request.getEmail());
            if (existingInvitation.isPresent()) {
                return responseUtils.badRequest("An invitation is already pending for this email");
            }

            var now = Instant.now();
            var token = UUID.randomUUID().toString();

            UserType userType = UserType.EMPLOYEE;
            if (request.getUserType() != null) {
                try {
                    userType = UserType.valueOf(request.getUserType().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }

            var invitation = UserInvitationEntity.builder()
                    .organizationId(organizationId)
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .userType(userType)
                    .roleIds(request.getRoleIds() != null ? request.getRoleIds() : List.of())
                    .departmentId(request.getDepartmentId())
                    .moduleIds(request.getModuleIds() != null ? request.getModuleIds() : List.of())
                    .message(request.getMessage())
                    .token(token)
                    .status("PENDING")
                    .invitedBy(invitedBy)
                    .invitedAt(now.toEpochMilli())
                    .expiresAt(now.plusSeconds(INVITATION_EXPIRY_DAYS * 24L * 3600L).toEpochMilli())
                    .build();

            var saved = invitationPort.save(invitation);

            var org = organizationService.getOrganizationById(organizationId);
            notificationService.sendEmail(
                    invitedBy,
                    organizationId,
                    "INVITATION",
                    saved.getId(),
                    SendEmailRequest.notificationEmail(
                            request.getEmail(),
                            buildRecipientName(request.getFirstName(), request.getLastName()),
                            "You've been invited to " + (org != null ? org.getName() : "an organization"),
                            request.getMessage() != null ? request.getMessage()
                                    : "You've been invited to join. Click the link to accept.",
                            buildInvitationLink(token)));

            return responseUtils.success(saved);
        } catch (AppException e) {
            markTransactionForRollback();
            log.error("Invite user failed: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            markTransactionForRollback();
            log.error("Invite user failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> acceptInvitation(String token, AcceptInvitationRequest request) {
        try {
            var invitationOpt = invitationPort.getByToken(token);
            if (invitationOpt.isEmpty()) {
                return responseUtils.badRequest("Invalid invitation token");
            }

            var invitation = invitationOpt.get();

            if (!invitation.isPending()) {
                return responseUtils.badRequest("This invitation has already been " + invitation.getStatus().toLowerCase());
            }

            if (invitation.isExpired()) {
                invitation.setStatus("EXPIRED");
                invitationPort.save(invitation);
                return responseUtils.badRequest("This invitation has expired");
            }

            var createRequest = CreateUserForOrgRequest.builder()
                    .firstName(invitation.getFirstName())
                    .lastName(invitation.getLastName())
                    .email(invitation.getEmail())
                    .password(request.getPassword())
                    .userType(invitation.getUserType())
                    .roleIds(invitation.getRoleIds())
                    .build();

            var createResponse = userUseCase.createUserForOrganization(
                    invitation.getOrganizationId(), createRequest);

            if (!createResponse.isSuccess()) {
                return createResponse;
            }

            invitation.setStatus("ACCEPTED");
            invitation.setAcceptedAt(Instant.now().toEpochMilli());
            invitationPort.save(invitation);

            return responseUtils.success("Invitation accepted successfully. You can now log in.");
        } catch (Exception e) {
            log.error("Accept invitation failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getInvitations(Long organizationId, String status, int page, int pageSize) {
        try {
            var result = (status != null && !status.isEmpty())
                    ? invitationPort.getByOrganizationIdAndStatus(organizationId, status, page, pageSize)
                    : invitationPort.getByOrganizationId(organizationId, page, pageSize);

            return responseUtils.success(paginationUtils.getResponse(
                    result.getFirst(), page, pageSize, result.getSecond()));
        } catch (Exception e) {
            log.error("Get invitations failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> cancelInvitation(Long organizationId, Long invitationId) {
        try {
            var invitationOpt = invitationPort.getById(invitationId, organizationId);
            if (invitationOpt.isEmpty()) {
                return responseUtils.badRequest("Invitation not found");
            }

            var invitation = invitationOpt.get();
            if (!invitation.isPending()) {
                return responseUtils.badRequest("Only pending invitations can be cancelled");
            }

            invitation.setStatus("CANCELLED");
            invitationPort.save(invitation);

            return responseUtils.success("Invitation cancelled successfully");
        } catch (Exception e) {
            log.error("Cancel invitation failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> resendInvitation(Long organizationId, Long invitationId) {
        try {
            var invitationOpt = invitationPort.getById(invitationId, organizationId);
            if (invitationOpt.isEmpty()) {
                return responseUtils.badRequest("Invitation not found");
            }

            var invitation = invitationOpt.get();
            if (!invitation.isPending() && !invitation.isExpired()) {
                return responseUtils.badRequest("Only pending or expired invitations can be resent");
            }

            invitation.setToken(UUID.randomUUID().toString());
            invitation.setStatus("PENDING");
            invitation.setExpiresAt(Instant.now().plusSeconds(INVITATION_EXPIRY_DAYS * 24L * 3600L).toEpochMilli());
            var saved = invitationPort.save(invitation);

            var org = organizationService.getOrganizationById(organizationId);
            notificationService.sendEmail(
                    invitation.getInvitedBy(),
                    organizationId,
                    "INVITATION",
                    saved.getId(),
                    SendEmailRequest.notificationEmail(
                            invitation.getEmail(),
                            buildRecipientName(invitation.getFirstName(), invitation.getLastName()),
                            "Reminder: You've been invited to " + (org != null ? org.getName() : "an organization"),
                            "This is a reminder to accept your invitation.",
                            buildInvitationLink(invitation.getToken())));

            return responseUtils.success(saved);
        } catch (Exception e) {
            markTransactionForRollback();
            log.error("Resend invitation failed: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private String buildInvitationLink(String token) {
        String frontendResetUrl = passwordResetProperties.getFrontendResetUrl();
        if (frontendResetUrl == null || frontendResetUrl.isBlank()) {
            return "http://localhost:3000/invitations/" + token + "/accept";
        }

        try {
            URI uri = URI.create(frontendResetUrl);
            if (uri.getScheme() != null && uri.getAuthority() != null) {
                return uri.getScheme() + "://" + uri.getAuthority() + "/invitations/" + token + "/accept";
            }
        } catch (Exception ignored) {
        }

        String normalizedUrl = frontendResetUrl.endsWith("/")
                ? frontendResetUrl.substring(0, frontendResetUrl.length() - 1)
                : frontendResetUrl;
        int authIndex = normalizedUrl.indexOf("/auth/reset-password");
        if (authIndex >= 0) {
            normalizedUrl = normalizedUrl.substring(0, authIndex);
        }
        return normalizedUrl + "/invitations/" + token + "/accept";
    }

    private String buildRecipientName(String firstName, String lastName) {
        String fullName = ((firstName == null ? "" : firstName.trim()) + " " + (lastName == null ? "" : lastName.trim()))
                .trim();
        return fullName.isBlank() ? "User" : fullName;
    }

    private void markTransactionForRollback() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}
