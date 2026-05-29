package serp.project.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import serp.project.payment_service.dto.webhook.FirstMilePaymentConfirmedWebhookRequest;
import serp.project.payment_service.entity.WebhookEvent;
import serp.project.payment_service.repository.WebhookEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private static final String PAYMENT_CONFIRMED_EVENT_TYPE = "ORDER_PAYMENT_CONFIRMED";
    private static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_ERROR_LENGTH = 1000;
    private static final int MAX_RESPONSE_LENGTH = 5000;

    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.webhook.first-mile.payment-confirmed-url:http://localhost:8093/api/v1/internal/payment-webhooks/orders/payment-confirmed}")
    private String firstMilePaymentConfirmedUrl;

    @Value("${app.webhook.first-mile.secret:}")
    private String firstMileWebhookSecret;

    @Value("${app.webhook.retry.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.webhook.retry.initial-delay-seconds:30}")
    private int initialDelaySeconds;

    @Transactional
    public void enqueueOrderPaymentConfirmedWebhook(FirstMilePaymentConfirmedWebhookRequest request) {
        if (request == null || request.getAppTransId() == null || request.getAppTransId().isBlank()) {
            log.warn("Skip enqueue webhook because request/appTransId is missing.");
            return;
        }

        String eventKey = PAYMENT_CONFIRMED_EVENT_TYPE + ":" + request.getAppTransId().trim();
        if (webhookEventRepository.existsByEventKey(eventKey)) {
            log.info("Webhook event {} already exists. Skip enqueue.", eventKey);
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize webhook payload for appTransId={}", request.getAppTransId(), e);
            return;
        }

        WebhookEvent event = WebhookEvent.builder()
                .eventKey(eventKey)
                .eventType(PAYMENT_CONFIRMED_EVENT_TYPE)
                .targetUrl(firstMilePaymentConfirmedUrl)
                .payload(payload)
                .status(WebhookEvent.WebhookStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(Math.max(1, maxAttempts))
                .nextRetryAt(LocalDateTime.now())
                .build();
        webhookEventRepository.save(event);

        // Try once immediately for fast propagation.
        dispatchEvent(event.getId());
    }

    @Transactional(readOnly = true)
    public List<Long> findRetryableEventIds(int batchSize) {
        int limit = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;
        List<Long> ids = webhookEventRepository.findRetryableEventIds(LocalDateTime.now());
        return ids.size() <= limit ? ids : ids.subList(0, limit);
    }

    public void processRetryableEvents(int batchSize) {
        List<Long> eventIds = findRetryableEventIds(batchSize);
        if (eventIds.isEmpty()) {
            return;
        }

        for (Long eventId : eventIds) {
            try {
                dispatchEvent(eventId);
            } catch (Exception ex) {
                log.error("Unexpected error while dispatching webhook eventId={}", eventId, ex);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dispatchEvent(Long eventId) {
        if (eventId == null) {
            return;
        }

        WebhookEvent event = webhookEventRepository.findByIdForUpdate(eventId).orElse(null);
        if (event == null) {
            return;
        }

        if (event.getStatus() == WebhookEvent.WebhookStatus.SUCCESS) {
            return;
        }

        int currentAttempt = event.getAttemptCount() == null ? 0 : event.getAttemptCount();
        int allowedAttempts = event.getMaxAttempts() == null ? Math.max(1, maxAttempts) : event.getMaxAttempts();
        if (currentAttempt >= allowedAttempts) {
            return;
        }

        int nextAttempt = currentAttempt + 1;
        event.setAttemptCount(nextAttempt);
        event.setLastAttemptAt(LocalDateTime.now());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (firstMileWebhookSecret != null && !firstMileWebhookSecret.isBlank()) {
                headers.set(WEBHOOK_SECRET_HEADER, firstMileWebhookSecret.trim());
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(event.getPayload(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    event.getTargetUrl(),
                    httpEntity,
                    String.class
            );

            int httpStatus = response.getStatusCode().value();
            String responseBody = truncate(response.getBody(), MAX_RESPONSE_LENGTH);
            event.setLastHttpStatus(httpStatus);
            event.setLastResponseBody(responseBody);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(WebhookEvent.WebhookStatus.SUCCESS);
                event.setDeliveredAt(LocalDateTime.now());
                event.setLastError(null);
                event.setNextRetryAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                log.info("Webhook delivered successfully eventKey={} attempt={}", event.getEventKey(), nextAttempt);
                return;
            }

            markFailed(event, String.format("Received non-success status: %s", httpStatus), nextAttempt, allowedAttempts);
        } catch (RestClientException ex) {
            markFailed(event, ex.getMessage(), nextAttempt, allowedAttempts);
        } catch (Exception ex) {
            markFailed(event, ex.getMessage(), nextAttempt, allowedAttempts);
        }
    }

    private void markFailed(
            WebhookEvent event,
            String rawError,
            int nextAttempt,
            int allowedAttempts
    ) {
        String error = truncate(rawError, MAX_ERROR_LENGTH);
        event.setStatus(WebhookEvent.WebhookStatus.FAILED);
        event.setLastError(error);

        if (nextAttempt < allowedAttempts) {
            event.setNextRetryAt(LocalDateTime.now().plusSeconds(computeBackoffSeconds(nextAttempt)));
        } else {
            // No further retry when attemptCount reaches maxAttempts.
            event.setNextRetryAt(LocalDateTime.now());
        }

        webhookEventRepository.save(event);
        log.warn(
                "Webhook delivery failed eventKey={} attempt={}/{} error={}",
                event.getEventKey(),
                nextAttempt,
                allowedAttempts,
                error
        );
    }

    private long computeBackoffSeconds(int attemptNumber) {
        long baseDelay = Math.max(1, initialDelaySeconds);
        long multiplier = 1L << Math.max(0, attemptNumber - 1);
        long backoff = baseDelay * multiplier;
        return Math.min(backoff, 1800L);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
