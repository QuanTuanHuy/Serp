/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardOrderVolumeResponse(
        long totalOrders,
        long newOrders,
        long inProgressOrders,
        long completedOrders,
        long cancelledOrders,
        long returnedOrders,
        double growthRatePercent
) {
}
