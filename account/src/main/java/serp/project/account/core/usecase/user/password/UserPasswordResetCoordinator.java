/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.password;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.INotificationService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.property.PasswordResetProperties;
import serp.project.account.kernel.utils.PasswordResetTokenUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserPasswordResetCoordinator {
    private final IUserService userService;
    private final INotificationService notificationService;
    private final PasswordResetProperties passwordResetProperties;

    @Transactional(rollbackFor = Exception.class)
    public void initiateReset(Long organizationId, Long userId, Long requestedBy) {
        var user = userService.getUserById(userId);
        if (user == null) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_FOUND, Constants.HttpStatusCode.BAD_REQUEST);
        }
        if (!Objects.equals(user.getPrimaryOrganizationId(), organizationId)) {
            throw new AppException(Constants.ErrorMessage.USER_NOT_IN_ORGANIZATION,
                    Constants.HttpStatusCode.FORBIDDEN);
        }
        if (!user.isActive()) {
            throw new AppException("Cannot reset password for inactive or suspended user",
                    Constants.HttpStatusCode.BAD_REQUEST);
        }
        if (user.getKeycloakId() == null) {
            throw new AppException("User does not have a Keycloak account", Constants.HttpStatusCode.BAD_REQUEST);
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new AppException("User does not have an email address", Constants.HttpStatusCode.BAD_REQUEST);
        }

        userService.invalidatePendingPasswordResetByUserId(userId);

        long expirationMinutes = passwordResetProperties.getExpirationMinutes();
        String rawToken = PasswordResetTokenUtils.generateToken();
        long expiresAt = Instant.now().plusSeconds(expirationMinutes * 60L).toEpochMilli();

        PasswordResetRequestEntity resetRequest = userService.createPasswordResetRequest(
                userId,
                organizationId,
                user.getEmail(),
                requestedBy,
                PasswordResetTokenUtils.hashToken(rawToken),
                expiresAt);

        SendEmailRequest emailRequest = SendEmailRequest.resetPasswordEmail(
                user.getEmail(),
                user.getFirstName(),
                buildPasswordResetLink(rawToken),
                expirationMinutes);
        notificationService.sendEmail(
                requestedBy,
                organizationId,
                "PASSWORD_RESET_REQUEST",
                resetRequest.getId(),
                emailRequest);
    }

    private String buildPasswordResetLink(String token) {
        String frontendResetUrl = passwordResetProperties.getFrontendResetUrl();
        if (frontendResetUrl == null || frontendResetUrl.isBlank()) {
            frontendResetUrl = "http://localhost:3000/auth/reset-password";
        }

        String separator = frontendResetUrl.contains("?") ? "&" : "?";
        return frontendResetUrl + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
