/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.OrderStatus;

import java.util.List;

public record OrderStatusTransitionResponse(
        String source,
        String idempotencyKey,
        List<Item> items
) {
    public record Item(
            Long orderId,
            String orderCode,
            OrderStatus previousStatus,
            OrderStatus targetStatus,
            OrderStatus currentStatus,
            boolean changed,
            String message
    ) {
    }
}
