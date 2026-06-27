/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.kernel.utils.TransactionAfterCommit;

@Component
@RequiredArgsConstructor
public class OrderEventDispatcher {

    private final OrderSyncEventPublisher orderSyncEventPublisher;
    private final OrderNotificationEventPublisher orderNotificationEventPublisher;

    public void publishOrderAfterCommit(Order order) {
        TransactionAfterCommit.run(() -> orderSyncEventPublisher.publish(order));
    }

    public void publishOrderConfirmedNotificationAfterCommit(Order order) {
        TransactionAfterCommit.run(() -> orderNotificationEventPublisher.publishOrderConfirmed(order));
    }

    public void publishOrderPaymentSucceededNotificationAfterCommit(Order order) {
        TransactionAfterCommit.run(() -> orderNotificationEventPublisher.publishOrderPaymentSucceeded(order));
    }

    public void publishOrderCancelledNotificationAfterCommit(Order order) {
        TransactionAfterCommit.run(() -> orderNotificationEventPublisher.publishOrderCancelled(order));
    }
}
