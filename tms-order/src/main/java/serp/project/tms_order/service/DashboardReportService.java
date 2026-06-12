/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.request.DashboardFilterRequest;
import serp.project.tms_order.dto.response.dashboard.DashboardAlertsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardLegsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardOverviewResponse;

public interface DashboardReportService {
    DashboardOverviewResponse getOverview(DashboardFilterRequest filterRequest, Long tenantId);

    DashboardLegsResponse getLegs(DashboardFilterRequest filterRequest, Long tenantId);

    DashboardAlertsResponse getAlerts(DashboardFilterRequest filterRequest, Long tenantId, int size);
}
