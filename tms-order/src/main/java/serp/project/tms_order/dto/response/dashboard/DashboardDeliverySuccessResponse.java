/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

import java.util.List;

public record DashboardDeliverySuccessResponse(
        long deliveredOrders,
        long failedDeliveryOrders,
        long returnedOrders,
        double successRatePercent,
        List<DashboardBreakdownItemResponse> failedReasons
) {
}
