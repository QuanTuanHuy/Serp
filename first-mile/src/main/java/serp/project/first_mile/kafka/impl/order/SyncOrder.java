/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */
package serp.project.first_mile.kafka.impl.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.kafka.KafkaProducer;
import serp.project.first_mile.kafka.event.OrderSyncEvent;

@Slf4j
@RequiredArgsConstructor
@Component
public class SyncOrder {
    @Value("${app.kafka.topics.sync-order:SYNC_ORDER}")
    private String syncOrderTopic;

    private final KafkaProducer kafkaProducer;

    public void sendOrderEvent(Order order) {
        OrderSyncEvent orderSyncEvent = OrderSyncEvent.builder()
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
                .build();
        String key = order.getOrderCode();
        kafkaProducer.sendMessageAsync(key, orderSyncEvent, syncOrderTopic, (success, sentTopic, payload, ex) -> {;
            if (success) {
                log.info("Published order sync event: orderCode={}, topic={}", key, sentTopic);
            } else {
                log.error("Failed to publish order sync event: orderCode={}, topic={}", key, sentTopic, ex);
            }
        });
    }
}
