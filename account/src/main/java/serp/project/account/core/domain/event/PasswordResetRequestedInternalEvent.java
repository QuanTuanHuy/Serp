/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.event;

public record PasswordResetRequestedInternalEvent(
        Long passwordResetRequestId,
        Long actorId,
        Long tenantId,
        String recipientEmail,
        String recipientName,
        String resetLink,
        Long expirationMinutes) {
    public PasswordResetRequestedInternalEvent {
        if (passwordResetRequestId == null) {
            throw new IllegalArgumentException("Password reset request id is required");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant id is required");
        }
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (resetLink == null || resetLink.isBlank()) {
            throw new IllegalArgumentException("Reset link is required");
        }
    }
}
