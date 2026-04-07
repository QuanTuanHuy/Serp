/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.request.AutoAssignPickupPlanRequest;
import serp.project.first_mile.dto.request.ManualAssignPickupOrdersRequest;
import serp.project.first_mile.dto.request.OptimizePickupPlanRequest;
import serp.project.first_mile.dto.response.PickupAssignmentResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;

public interface PickupOptimizationService {
    PickupOptimizationResponse optimizePickupPlan(OptimizePickupPlanRequest request);

    PickupAssignmentResponse autoAssignPickupPlan(AutoAssignPickupPlanRequest request);

    PickupAssignmentResponse manualAssignPickupOrders(ManualAssignPickupOrdersRequest request);
}
