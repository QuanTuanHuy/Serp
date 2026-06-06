package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.CapacityUtilizationReportResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;

public interface IReportingService {

    OperationalReportResponse getOperationsSummary(ReportFilterParamsRequest params, Long tenantId);

    String exportOperationsSummaryCsv(ReportFilterParamsRequest params, Long tenantId);

    PageResponse<TripExecutionResponse> getTripsReport(ReportFilterParamsRequest params, Long tenantId);

    PageResponse<AttendanceResponse> getAttendanceReport(ReportFilterParamsRequest params, Long tenantId);

    PageResponse<CapacityUtilizationReportResponse> getCapacityUtilization(ReportFilterParamsRequest params,
            Long tenantId);
}
