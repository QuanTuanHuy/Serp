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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import serp.project.second_mile.domain.Order;
import serp.project.second_mile.enums.OrderSyncEventSource;
import serp.project.second_mile.kafka.event.OrderSyncEvent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSyncEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.sync-order:SYNC_ORDER}")
    private String topic;

    public void publish(Order order) {
        if (order == null || order.getOrderCode() == null || order.getOrderCode().isBlank()) {
            log.warn("Skip order sync publish: invalid order {}", order);
            return;
        }
        publishEvent(toEvent(order));
    }

    public void publishAll(Collection<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (Order order : orders) {
            publish(order);
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
                .orderProductCategory(order.getOrderProductCategory())
                .orderType(order.getOrderType())
                .totalWeight(order.getTotalWeight())
                .totalVolume(order.getTotalVolume())
                .dimensions(order.getDimensions())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .tenantId(order.getTenantId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .eventSource(OrderSyncEventSource.SECOND_MILE)
                .build();
    }

    private void publishEvent(OrderSyncEvent event) {
        final String key = event.getOrderCode();
        final String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderSyncEvent orderCode={}: {}", key, e.getMessage(), e);
            return;
        }
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send order sync topic={} key={}: {}", topic, key, ex.getMessage(), ex);
            } else if (result != null && result.getRecordMetadata() != null) {
                log.info("Order sync sent topic={} partition={} offset={} key={} status={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        event.getStatus());
            }
        });
    }
}
