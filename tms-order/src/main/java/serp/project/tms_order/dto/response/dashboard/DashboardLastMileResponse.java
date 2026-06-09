/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardLastMileResponse(
        long totalOrders,
        double deliverySuccessRatePercent,
        Long avgDeliveryMinutes,
        List<DashboardBreakdownItemResponse> failedReasons,
        long slaBreachedOrders,
        List<DashboardTrendPointResponse> trend,
        List<DashboardBreakdownItemResponse> statusBreakdown
) {
}
