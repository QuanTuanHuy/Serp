/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardMiddleMileResponse(
        long totalOrders,
        long totalBags,
        long totalRoutes,
        double onTimeRouteRatePercent,
        Long avgHubDwellMinutes,
        List<DashboardBreakdownItemResponse> statusBreakdown,
        List<DashboardTopEntityResponse> topHubs
) {
}
