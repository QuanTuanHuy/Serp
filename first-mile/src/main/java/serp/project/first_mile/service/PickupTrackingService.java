/*
Author: GitHub Copilot
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;

import java.time.LocalDate;

public interface PickupTrackingService {

    PickupTrackingOverviewResponse getPickupTrackingOverview(
            LocalDate tripDate,
            Long postOfficeId,
            Long courierStaffId
    );
}
