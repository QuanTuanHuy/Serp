/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.port.store.IEmailPort;
import serp.project.mailservice.core.usecase.EmailSendingUseCases;
import serp.project.mailservice.kernel.property.RetryProperties;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryScheduler {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final IEmailPort emailPort;
    private final RetryProperties retryProperties;
    private final EmailSendingUseCases emailSendingUseCases;

    @Scheduled(cron = "${app.email.scheduler.retry-failed-emails-cron:0 */5 * * * *}")
    public void retryFailedEmails() {
        if (!Boolean.TRUE.equals(retryProperties.getEnabled())) {
            return;
        }

        int maxAttempts = retryProperties.getMaxAttempts() != null
                ? retryProperties.getMaxAttempts()
                : 3;

        List<EmailEntity> retryCandidates = emailPort.findEmailsForRetry(
                EmailStatus.RETRY,
                LocalDateTime.now(),
                maxAttempts,
                DEFAULT_BATCH_SIZE
        );

        if (retryCandidates.isEmpty()) {
            return;
        }

        log.info("Found {} emails ready for retry", retryCandidates.size());

        for (EmailEntity email : retryCandidates) {
            try {
                emailSendingUseCases.resendFailedEmail(email.getMessageId(), email.getTenantId());
            } catch (Exception ex) {
                log.error("Retry failed for messageId: {}", email.getMessageId(), ex);
            }
        }
    }
}
