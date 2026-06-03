/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller.dto.tms_order;

import serp.project.second_mile.enums.OrderStatus;

import java.util.List;

public record TmsOrderStatusTransitionResponse(
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
