/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.kafka.event.TmsOrderStatusTransitionEvent;
import serp.project.first_mile.kernel.utils.JsonUtils;
import serp.project.first_mile.service.OutboxEventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmsOrderStatusTransitionEventPublisher {
    private static final String AGGREGATE_TYPE_TMS_ORDER = "TMS_ORDER";
    private static final String EVENT_TYPE_ORDER_STATUS_TRANSITION = "tms-order.status.transition";

    private final JsonUtils jsonUtils;
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
        outboxEventService.enqueue(
                AGGREGATE_TYPE_TMS_ORDER,
                request.getIdempotencyKey().trim(),
                EVENT_TYPE_ORDER_STATUS_TRANSITION,
                topic,
                key,
                jsonUtils.toJson(event),
                tenantId
        );
        log.info("Enqueued TMS order transition outbox event key={} topic={}", key, topic);
    }
}
