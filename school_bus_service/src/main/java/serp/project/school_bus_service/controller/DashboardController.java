package serp.project.school_bus_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.CapacityUtilizationReportResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import serp.project.school_bus_service.dto.params.DashboardFilterParamsRequest;
import serp.project.school_bus_service.dto.response.ChartItemDto;
import serp.project.school_bus_service.dto.response.DashboardOperationsResponse;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.DropdownOptionResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.ReportOverviewResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.service.IDashboardService;
import serp.project.school_bus_service.service.IReportingService;
import serp.project.school_bus_service.shared.auth.AuthUtils;
import serp.project.school_bus_service.shared.base.AbstractBaseController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController extends AbstractBaseController {

    private final IDashboardService dashboardService;
    private final IReportingService reportingService;

    public DashboardController(IDashboardService dashboardService, IReportingService reportingService, AuthUtils authUtils) {
        super(authUtils);
        this.dashboardService = dashboardService;
        this.reportingService = reportingService;
    }

    @GetMapping("/summary")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<DashboardSummaryResponse>> getSummary(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched dashboard summary", dashboardService.getSummary(params, getCurrentTenantId()));
    }

    @GetMapping("/charts/trip-status")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<ChartItemDto>>> getTripStatusChart(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched trip status chart", dashboardService.getTripStatusChart(params, getCurrentTenantId()));
    }

    @GetMapping("/charts/attendance-status")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<ChartItemDto>>> getAttendanceStatusChart(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched attendance status chart",
                dashboardService.getAttendanceStatusChart(params, getCurrentTenantId()));
    }

    @GetMapping("/charts/route-readiness")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<ChartItemDto>>> getRouteReadinessChart(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched route readiness chart",
                dashboardService.getRouteReadinessChart(params, getCurrentTenantId()));
    }

    @GetMapping("/charts/request-status")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<ChartItemDto>>> getRequestStatusChart(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched request status chart",
                dashboardService.getRequestStatusChart(params, getCurrentTenantId()));
    }

    @GetMapping("/charts/trips-by-date")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<ChartItemDto>>> getTripsByDateChart(
            @ModelAttribute DashboardFilterParamsRequest params) {
        return ok("Fetched trips by date chart",
                dashboardService.getTripsByDateChart(params, getCurrentTenantId()));
    }

    @GetMapping("/schools")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    public ResponseEntity<GeneralResponse<List<DropdownOptionResponse>>> getDashboardSchools() {
        return ok("Fetched dashboard schools", dashboardService.getDashboardSchools(getCurrentTenantId()));
    }

    @GetMapping("/operations")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.dashboard.read')")
    @Deprecated
    public ResponseEntity<GeneralResponse<DashboardOperationsResponse>> getOperationsDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate serviceDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String direction) {
        return ok("Fetched operations dashboard metrics",
                dashboardService.getOperationsDashboard(serviceDate, fromDate, toDate, schoolId, direction, getCurrentTenantId()));
    }


    @GetMapping("/reports/operations-summary")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<OperationalReportResponse>> getOperationsReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched operations report", reportingService.getOperationsSummary(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/overview")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<ReportOverviewResponse>> getReportOverview(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched report overview", reportingService.getReportOverview(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/operations-summary/export")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.export')")
    public ResponseEntity<String> exportOperationsReport(@ModelAttribute ReportFilterParamsRequest params) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=school-bus-operations-summary.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(reportingService.exportOperationsSummaryCsv(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/trips")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<TripExecutionResponse>>> getTripsReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched trip report", reportingService.getTripsReport(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/attendance")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<AttendanceResponse>>> getAttendanceReport(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched attendance report", reportingService.getAttendanceReport(params, getCurrentTenantId()));
    }

    @GetMapping("/reports/capacity-utilization")
    @PreAuthorize("@roleAuthorizer.hasPermission('school-bus.report.read')")
    public ResponseEntity<GeneralResponse<PageResponse<CapacityUtilizationReportResponse>>> getCapacityUtilization(
            @ModelAttribute ReportFilterParamsRequest params) {
        return ok("Fetched capacity utilization report",
                reportingService.getCapacityUtilization(params, getCurrentTenantId()));
    }
}
