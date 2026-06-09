/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardPeriodResponse(
        String fromDate,
        String toDate,
        String timezone,
        String granularity
) {
}
