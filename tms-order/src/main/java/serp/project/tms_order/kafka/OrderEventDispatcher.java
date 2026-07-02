/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_order.domain.Order;

@Component
@RequiredArgsConstructor
public class OrderEventDispatcher {

    private final OrderSyncEventPublisher orderSyncEventPublisher;
    private final OrderNotificationEventPublisher orderNotificationEventPublisher;

    public void publishOrderAfterCommit(Order order) {
        orderSyncEventPublisher.publish(order);
    }

    public void publishOrderConfirmedNotificationAfterCommit(Order order) {
        orderNotificationEventPublisher.publishOrderConfirmed(order);
    }

    public void publishOrderPaymentSucceededNotificationAfterCommit(Order order) {
        orderNotificationEventPublisher.publishOrderPaymentSucceeded(order);
    }

    public void publishOrderCancelledNotificationAfterCommit(Order order) {
        orderNotificationEventPublisher.publishOrderCancelled(order);
    }
}
