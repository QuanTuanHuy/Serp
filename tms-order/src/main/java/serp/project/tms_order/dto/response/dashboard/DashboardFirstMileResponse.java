/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardFirstMileResponse(
        long totalOrders,
        double pickupSuccessRatePercent,
        Long avgPickupMinutes,
        long slaBreachedOrders,
        List<DashboardBreakdownItemResponse> statusBreakdown,
        List<DashboardTrendPointResponse> trend,
        List<DashboardTopEntityResponse> topPostOffices
) {
}
