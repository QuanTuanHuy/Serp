/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.dto.request.SortInboundOrdersRequest;
import serp.project.first_mile.dto.response.InboundOrderResponse;
import serp.project.first_mile.enums.OrderStatus;

import java.util.List;

public interface OrderSortingService {
    List<InboundOrderResponse> getInboundOrders(String postOfficeCode, OrderStatus status, Long tenantId);

    List<InboundOrderResponse> confirmInbound(SortInboundOrdersRequest request, Long tenantId);
}
