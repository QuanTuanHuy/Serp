/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.response.PickupCheckinDetailResponse;
import serp.project.first_mile.dto.response.PickupTripLifecycleResponse;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;

import java.time.LocalDate;

public interface PickupTrackingService {

    PickupTrackingOverviewResponse getPickupTrackingOverview(
            LocalDate tripDate,
            Long postOfficeId,
            Long courierStaffId
    );

    PickupCheckinDetailResponse getPickupCheckinDetail(Long orderId, Long tenantId);

    PickupTripLifecycleResponse completeTrip(Long tripId, Long tenantId);

    PickupTripLifecycleResponse returnTripToPostOffice(Long tripId, Long tenantId);
}
