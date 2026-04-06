package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.application.dto.response.OperationalReportResponse;

public interface IDashboardService {

    DashboardSummaryResponse getSummary(Long tenantId);

    OperationalReportResponse getOperationsReport(Long tenantId);
}
