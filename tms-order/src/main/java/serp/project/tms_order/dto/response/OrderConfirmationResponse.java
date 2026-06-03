/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

import serp.project.tms_order.enums.OrderStatus;

public record OrderConfirmationResponse(
        Long orderId,
        String orderCode,
        String customerOrderCode,
        OrderStatus status,
        boolean alreadyConfirmed,
        OriginPostOfficeInfo originPostOffice
) {

    public record OriginPostOfficeInfo(
            Long id,
            String code,
            String name,
            Integer currentLoad,
            Integer dailyCapacity
    ) {
    }
}
