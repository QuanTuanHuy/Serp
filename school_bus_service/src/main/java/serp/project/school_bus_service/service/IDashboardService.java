package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DashboardOperationsResponse;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;

import java.time.LocalDate;

public interface IDashboardService {

    DashboardSummaryResponse getSummary(Long tenantId);

    OperationalReportResponse getOperationsReport(Long tenantId);

    DashboardOperationsResponse getOperationsDashboard(
            LocalDate serviceDate,
            LocalDate fromDate,
            LocalDate toDate,
            Long schoolId,
            String direction,
            Long tenantId);
}

