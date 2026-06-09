/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.enums.OrderSyncEventSource;
import serp.project.tms_order.kafka.event.OrderSyncEvent;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.sync-order:SYNC_ORDER}")
    private String syncOrderTopic;

    public void publish(Order order) {
        if (order == null || order.getOrderCode() == null) {
            log.warn("Skip publishing order sync event because order or orderCode is null");
            return;
        }

        OrderSyncEvent event = toEvent(order);
        String key = order.getOrderCode();
        try {
            String payload = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(syncOrderTopic, key, payload);
            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Failed to publish order sync event orderCode={} topic={}",
                            key, syncOrderTopic, exception);
                    return;
                }
                log.info("Published order sync event orderCode={} topic={}", key, syncOrderTopic);
            });
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize order sync event orderCode={}", key, exception);
        }
    }

    private OrderSyncEvent toEvent(Order order) {
        return OrderSyncEvent.builder()
                .orderCode(order.getOrderCode())
                .customerOrderCode(order.getCustomerOrderCode())
                .originPostOfficeCode(order.getOriginPostOfficeCode())
                .destinationPostOfficeCode(order.getDestinationPostOfficeCode())
                .status(order.getStatus())
                .note(order.getNote())
                .orderType(order.getOrderType())
                .totalWeight(order.getTotalWeight())
                .totalVolume(order.getTotalVolume())
                .dimensions(order.getDimensions())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .tenantId(order.getTenantId())
                .eventSource(OrderSyncEventSource.TMS_ORDER)
                .build();
    }
}
