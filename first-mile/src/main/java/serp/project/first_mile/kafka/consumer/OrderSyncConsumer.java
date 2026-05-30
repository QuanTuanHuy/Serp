/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.first_mile.kafka.event.OrderSyncEvent;
import serp.project.first_mile.service.KafkaDlqService;
import serp.project.first_mile.service.OrderInboundSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSyncConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaDlqService kafkaDlqService;
    private final OrderInboundSyncService orderInboundSyncService;

    @KafkaListener(
            topics = "${app.kafka.topics.sync-order:SYNC_ORDER}",
            groupId = "${app.kafka.order-sync.consumer-group-id:first-mile-sync-order}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            OrderSyncEvent event = objectMapper.readValue(payload, OrderSyncEvent.class);
            orderInboundSyncService.applyInboundStatus(event);
            log.info("Consumed sync-order inbound event: topic={}, key={}, orderCode={}, tenantId={}, source={}",
                    topic,
                    key,
                    event.getOrderCode(),
                    event.getTenantId(),
                    event.getEventSource());
        } catch (Exception exception) {
            Long tenantId = extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(topic, key, payload, exception.getMessage(), tenantId);
            log.error(
                    "Failed to consume sync-order event and moved to DLQ: topic={}, key={}, tenantId={}, payload={}",
                    topic,
                    key,
                    tenantId,
                    payload,
                    exception
            );
        }
    }

    private Long extractTenantId(String payload) {
        try {
            OrderSyncEvent event = objectMapper.readValue(payload, OrderSyncEvent.class);
            return event.getTenantId();
        } catch (Exception exception) {
            return null;
        }
    }
}
