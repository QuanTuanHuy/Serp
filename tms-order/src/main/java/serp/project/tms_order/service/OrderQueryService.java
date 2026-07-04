/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.request.InternalOrderLookupRequest;
import serp.project.tms_order.dto.request.InternalPickupCandidateRequest;
import serp.project.tms_order.dto.response.OrderOperationView;

import java.util.List;

public interface OrderQueryService {
    List<OrderOperationView> lookupOrders(InternalOrderLookupRequest request, Long tenantId);

    List<OrderOperationView> findPickupCandidates(InternalPickupCandidateRequest request, Long tenantId);
}
