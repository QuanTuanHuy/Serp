package serp.project.payment_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import serp.project.payment_service.service.WebhookEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRetryScheduler {

    private final WebhookEventService webhookEventService;

    @Scheduled(fixedDelayString = "${app.webhook.retry.scheduler-delay-ms:30000}")
    public void retryWebhookEvents() {
        try {
            webhookEventService.processRetryableEvents(100);
        } catch (Exception ex) {
            log.error("Webhook retry scheduler failed: {}", ex.getMessage(), ex);
        }
    }
}
