package serp.project.school_bus_service.ui.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.CapacityUtilizationReportResponse;
import serp.project.school_bus_service.application.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.core.service.IDashboardService;
import serp.project.school_bus_service.core.service.IReportingService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

@RestController
@RequestMapping
public class DashboardController extends AbstractBaseController {

    private final IDashboardService dashboardService;
    private final IReportingService reportingService;

    public DashboardController(IDashboardService dashboardService, IReportingService reportingService, AuthUtils authUtils) {
        super(authUtils);
        this.dashboardService = dashboardService;
        this.reportingService = reportingService;
    }

    @GetMapping("/dashboard/summary")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<DashboardSummaryResponse>> getSummary() {
        return ok("Fetched dashboard summary", dashboardService.getSummary(getCurrentTenantId()));
    }

    @GetMapping("/reports/operations-summary")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<OperationalReportResponse>> getOperationsReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched operations report", reportingService.getOperationsSummary(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/operations-summary/export")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<String> exportOperationsReport(@ModelAttribute ReportFilterParamsRequest params) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=school-bus-operations-summary.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(reportingService.exportOperationsSummaryCsv(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/trips")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<TripExecutionResponse>>> getTripsReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched trip report", reportingService.getTripsReport(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/attendance")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendanceResponse>>> getAttendanceReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched attendance report", reportingService.getAttendanceReport(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/capacity-utilization")
    // @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<CapacityUtilizationReportResponse>>> getCapacityUtilization(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched capacity utilization report",
                reportingService.getCapacityUtilization(params, getCurrentTenantId()));
    }
}
