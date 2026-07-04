/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardOverviewResponse(
        DashboardScopeResponse scope,
        DashboardPeriodResponse period,
        DashboardOrderVolumeResponse orderVolume,
        List<DashboardStatusResponse> orderStatuses,
        DashboardDeliverySuccessResponse deliverySuccess,
        DashboardFinanceResponse finance,
        String lastUpdatedAt
) {
}
