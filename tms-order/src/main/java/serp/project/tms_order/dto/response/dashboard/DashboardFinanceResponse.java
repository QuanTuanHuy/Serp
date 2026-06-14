/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardFinanceResponse(
        long grossRevenue,
        long netRevenue,
        long codAmount,
        long codCollected,
        long codReconciled,
        long codPending,
        String currency,
        boolean estimated,
        String source
) {
}
