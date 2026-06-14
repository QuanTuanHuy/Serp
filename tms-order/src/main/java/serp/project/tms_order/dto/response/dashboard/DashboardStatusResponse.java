/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardStatusResponse(
        String statusCode,
        String statusName,
        long count,
        double percentage,
        String lastUpdatedAt
) {
}
