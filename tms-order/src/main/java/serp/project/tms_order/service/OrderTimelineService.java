/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.dto.response.OrderTimelineResponse;
import serp.project.tms_order.enums.OrderStatus;

import java.util.List;

public interface OrderTimelineService {
    void recordStatusEvent(
            Order order,
            OrderStatus orderStatus,
            String description,
            InternalOrderStatusTransitionRequest.Context context
    );

    List<OrderTimelineResponse> getTimeline(Long orderId, Long tenantId);
}
