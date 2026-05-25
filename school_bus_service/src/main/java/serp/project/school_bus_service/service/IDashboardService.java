package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;

public interface IDashboardService {

    DashboardSummaryResponse getSummary(Long tenantId);

    OperationalReportResponse getOperationsReport(Long tenantId);
}
