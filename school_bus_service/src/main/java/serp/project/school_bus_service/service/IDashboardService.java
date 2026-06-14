package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.params.DashboardFilterParamsRequest;
import serp.project.school_bus_service.dto.response.ChartItemDto;
import serp.project.school_bus_service.dto.response.DashboardOperationsResponse;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.DropdownOptionResponse;

import java.time.LocalDate;
import java.util.List;

public interface IDashboardService {

    DashboardSummaryResponse getSummary(DashboardFilterParamsRequest params, Long tenantId);

    List<ChartItemDto> getTripStatusChart(DashboardFilterParamsRequest params, Long tenantId);

    List<ChartItemDto> getAttendanceStatusChart(DashboardFilterParamsRequest params, Long tenantId);

    List<ChartItemDto> getRouteReadinessChart(DashboardFilterParamsRequest params, Long tenantId);

    List<ChartItemDto> getRequestStatusChart(DashboardFilterParamsRequest params, Long tenantId);

    List<ChartItemDto> getTripsByDateChart(DashboardFilterParamsRequest params, Long tenantId);

    List<DropdownOptionResponse> getDashboardSchools(Long tenantId);

    DashboardOperationsResponse getOperationsDashboard(
            LocalDate serviceDate,
            LocalDate fromDate,
            LocalDate toDate,
            Long schoolId,
            String direction,
            Long tenantId);
}

