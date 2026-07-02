/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.tms_order.kafka.OrderStatusTransitionEventProcessor;
import serp.project.tms_order.service.KafkaDlqService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusTransitionConsumer {

    private final KafkaDlqService kafkaDlqService;
    private final OrderStatusTransitionEventProcessor eventProcessor;

    @KafkaListener(
            topics = "${app.kafka.topics.order-status-transition:ORDER_STATUS_TRANSITIONS}",
            groupId = "${app.kafka.order-status-transition.consumer-group-id:tms-order-status-transition}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        try {
            Long tenantId = eventProcessor.process(payload);
            log.info("Consumed order status transition event: topic={}, key={}, tenantId={}",
                    topic,
                    key,
                    tenantId);
        } catch (Exception exception) {
            Long tenantId = eventProcessor.extractTenantId(payload);
            kafkaDlqService.saveFailedMessage(topic, key, payload, exception.getMessage(), tenantId);
            log.error("Failed to consume order status transition event and moved to DLQ: topic={}, key={}, tenantId={}",
                    topic,
                    key,
                    tenantId,
                    exception);
        }
    }
}
