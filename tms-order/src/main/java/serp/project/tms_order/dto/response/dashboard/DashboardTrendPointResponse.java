/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardTrendPointResponse(
        String label,
        String date,
        long value
) {
}
