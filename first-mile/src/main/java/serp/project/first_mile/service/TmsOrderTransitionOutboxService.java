/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;

public interface TmsOrderTransitionOutboxService {
    void enqueue(TmsOrderStatusTransitionRequest request, Long tenantId);

    void processDueTransitions();
}
