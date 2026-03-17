/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.message.SendEmailRequest;
import serp.project.account.core.domain.event.PasswordResetRequestedInternalEvent;
import serp.project.account.core.service.INotificationService;

@Component
@Slf4j
public class PasswordResetInternalEventListener {
    private static final String AGGREGATE_TYPE = "PASSWORD_RESET_REQUEST";

    private final INotificationService notificationService;

    public PasswordResetInternalEventListener(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePasswordResetRequested(PasswordResetRequestedInternalEvent event) {
        SendEmailRequest request = SendEmailRequest.resetPasswordEmail(
                event.recipientEmail(),
                event.recipientName(),
                event.resetLink(),
                event.expirationMinutes());

        notificationService.sendEmail(
                event.actorId(),
                event.tenantId(),
                AGGREGATE_TYPE,
                event.passwordResetRequestId(),
                request);

        log.info("Queued password reset outbox event for request {}", event.passwordResetRequestId());
    }
}
