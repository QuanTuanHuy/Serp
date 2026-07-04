/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.kafka.event.TmsOrderStatusTransitionEvent;
import serp.project.second_mile.service.OutboxEventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmsOrderStatusTransitionEventPublisher {
    private static final String AGGREGATE_TYPE_TMS_ORDER = "TMS_ORDER";
    private static final String EVENT_TYPE_ORDER_STATUS_TRANSITION = "tms-order.status.transition";

    private final ObjectMapper objectMapper;
    private final OutboxEventService outboxEventService;

    @Value("${app.kafka.topics.order-status-transition:ORDER_STATUS_TRANSITIONS}")
    private String topic;

    public void publish(TmsOrderStatusTransitionRequest request, Long tenantId) {
        if (request == null || tenantId == null || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().isBlank()) {
            log.warn("Skip TMS order transition publish: invalid request tenantId={}", tenantId);
            return;
        }

        TmsOrderStatusTransitionEvent event = TmsOrderStatusTransitionEvent.builder()
                .tenantId(tenantId)
                .request(request)
                .build();
        String key = tenantId + ":" + request.getIdempotencyKey().trim();

        final String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize TMS order transition event: {}", exception.getMessage(), exception);
            return;
        }

        outboxEventService.enqueue(
                AGGREGATE_TYPE_TMS_ORDER,
                request.getIdempotencyKey().trim(),
                EVENT_TYPE_ORDER_STATUS_TRANSITION,
                topic,
                key,
                json,
                tenantId
        );
        log.info("Enqueued TMS order transition outbox event key={} topic={}", key, topic);
    }
}
