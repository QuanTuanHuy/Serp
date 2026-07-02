/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
import serp.project.first_mile.enums.PickupShift;

import java.time.LocalDate;

public interface DeliveryDispatchService {
    DeliveryAssignmentResponse autoAssignDeliveryPlan(AutoAssignDeliveryPlanRequest request);

    DeliveryAssignmentResponse getDeliveryTrips(Long postOfficeId, PickupShift shift, LocalDate tripDate);

    DeliveryScanOutResponse scanOutDeliveryOrder(Long tripId, ScanOutDeliveryOrderRequest request);
}
