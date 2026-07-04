/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;

public interface TmsOrderTransitionPublisherService {
    void publish(TmsOrderStatusTransitionRequest request, Long tenantId);
}
