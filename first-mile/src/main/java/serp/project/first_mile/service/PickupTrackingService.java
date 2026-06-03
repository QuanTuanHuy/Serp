/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.ConfirmPostOfficeInboundRequest;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
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

    PickupCheckinResponse checkInPickupOrder(
            Long orderId,
            Double checkinLatitude,
            Double checkinLongitude,
            MultipartFile photo,
            Long tenantId
    );

    PickupCheckinDetailResponse getPickupCheckinDetail(Long orderId, Long tenantId);

    PickupTripLifecycleResponse completeTrip(Long tripId, Long tenantId);

    PickupTripLifecycleResponse returnTripToPostOffice(Long tripId, Long tenantId);

    PickupTripLifecycleResponse confirmPostOfficeInbound(
            Long tripId,
            ConfirmPostOfficeInboundRequest request,
            Long tenantId
    );
}
