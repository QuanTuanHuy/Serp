/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response.dashboard;

public record DashboardLegsResponse(
        DashboardScopeResponse scope,
        DashboardPeriodResponse period,
        DashboardFirstMileResponse firstMile,
        DashboardMiddleMileResponse middleMile,
        DashboardLastMileResponse lastMile,
        String lastUpdatedAt
) {
}
