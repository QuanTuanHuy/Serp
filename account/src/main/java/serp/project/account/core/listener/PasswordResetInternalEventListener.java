/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.event.PasswordResetRequestedInternalEvent;
import serp.project.account.core.service.INotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetInternalEventListener {
    private static final String AGGREGATE_TYPE = "PASSWORD_RESET_REQUEST";

    private final INotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetRequested(PasswordResetRequestedInternalEvent event) {
        try {
            SendEmailRequest request = SendEmailRequest.resetPasswordEmail(
                    event.recipientEmail(),
                    event.recipientName(),
                    event.resetLink(),
                    event.expirationMinutes());

            notificationService.sendEmail(
                    event.actorId(),
                    event.tenantId(),
                    AGGREGATE_TYPE,
                    event.passwordResetRequestId().toString(),
                    request);
        } catch (Exception e) {
            log.error("Failed to publish password reset email event for request {}: {}",
                    event.passwordResetRequestId(),
                    e.getMessage(),
                    e);
        }
    }
}
