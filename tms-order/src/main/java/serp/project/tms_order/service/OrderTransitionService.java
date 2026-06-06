/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.dto.response.OrderStatusTransitionResponse;

public interface OrderTransitionService {
    OrderStatusTransitionResponse applyTransitions(
            InternalOrderStatusTransitionRequest request,
            Long tenantId
    );
}
