/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardAlertsResponse(
        DashboardScopeResponse scope,
        DashboardPeriodResponse period,
        List<DashboardAlertResponse> items,
        String lastUpdatedAt
) {
}
