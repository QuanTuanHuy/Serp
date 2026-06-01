/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.domain.Order;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.util.List;

public interface OrderTimelineService {

    void recordStatusEvent(
            Order order,
            OrderStatus orderStatus,
            String description,
            OrderTimelineContext context
    );

    List<OrderTimelineResponse> getTimeline(Long orderId, Long tenantId);
}
