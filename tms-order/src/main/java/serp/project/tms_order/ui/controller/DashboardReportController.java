/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.request.DashboardFilterRequest;
import serp.project.tms_order.dto.response.dashboard.DashboardAlertsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardLegsResponse;
import serp.project.tms_order.dto.response.dashboard.DashboardOverviewResponse;
import serp.project.tms_order.enums.OrderType;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.DashboardReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tms/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardReportController {
    private final AuthUtils authUtils;
    private final MessageService messageService;
    private final DashboardReportService dashboardReportService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_HUB_MANAGER')")
    public ApiResponse<DashboardOverviewResponse> getOverview(
            @RequestParam(name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "Asia/Saigon") String timezone,
            @RequestParam(defaultValue = "DAY") String granularity,
            @RequestParam(name = "hubId", required = false) Long hubId,
            @RequestParam(name = "postOfficeId", required = false) Long postOfficeId,
            @RequestParam(name = "postOfficeCode", required = false) String postOfficeCode,
            @RequestParam(name = "postOfficeCodes", required = false) List<String> postOfficeCodes,
            @RequestParam(name = "serviceType", required = false) OrderType serviceType
    ) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to get TMS dashboard overview for tenant {}", tenantId);
        return ApiResponse.<DashboardOverviewResponse>builder()
                .message(messageService.getMessage("success.dashboard.overview"))
                .result(dashboardReportService.getOverview(buildFilter(
                        fromDate,
                        toDate,
                        timezone,
                        granularity,
                        hubId,
                        postOfficeId,
                        postOfficeCode,
                        postOfficeCodes,
                        serviceType
                ), tenantId))
                .build();
    }

    @GetMapping("/legs")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_HUB_MANAGER')")
    public ApiResponse<DashboardLegsResponse> getLegs(
            @RequestParam(name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "Asia/Saigon") String timezone,
            @RequestParam(defaultValue = "DAY") String granularity,
            @RequestParam(name = "hubId", required = false) Long hubId,
            @RequestParam(name = "postOfficeId", required = false) Long postOfficeId,
            @RequestParam(name = "postOfficeCode", required = false) String postOfficeCode,
            @RequestParam(name = "postOfficeCodes", required = false) List<String> postOfficeCodes,
            @RequestParam(name = "serviceType", required = false) OrderType serviceType
    ) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to get TMS dashboard legs for tenant {}", tenantId);
        return ApiResponse.<DashboardLegsResponse>builder()
                .message(messageService.getMessage("success.dashboard.legs"))
                .result(dashboardReportService.getLegs(buildFilter(
                        fromDate,
                        toDate,
                        timezone,
                        granularity,
                        hubId,
                        postOfficeId,
                        postOfficeCode,
                        postOfficeCodes,
                        serviceType
                ), tenantId))
                .build();
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_HUB_MANAGER')")
    public ApiResponse<DashboardAlertsResponse> getAlerts(
            @RequestParam(name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "Asia/Saigon") String timezone,
            @RequestParam(defaultValue = "DAY") String granularity,
            @RequestParam(name = "hubId", required = false) Long hubId,
            @RequestParam(name = "postOfficeId", required = false) Long postOfficeId,
            @RequestParam(name = "postOfficeCode", required = false) String postOfficeCode,
            @RequestParam(name = "postOfficeCodes", required = false) List<String> postOfficeCodes,
            @RequestParam(name = "serviceType", required = false) OrderType serviceType,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to get TMS dashboard alerts for tenant {}", tenantId);
        return ApiResponse.<DashboardAlertsResponse>builder()
                .message(messageService.getMessage("success.dashboard.alerts"))
                .result(dashboardReportService.getAlerts(buildFilter(
                        fromDate,
                        toDate,
                        timezone,
                        granularity,
                        hubId,
                        postOfficeId,
                        postOfficeCode,
                        postOfficeCodes,
                        serviceType
                ), tenantId, size))
                .build();
    }

    private DashboardFilterRequest buildFilter(
            LocalDate fromDate,
            LocalDate toDate,
            String timezone,
            String granularity,
            Long hubId,
            Long postOfficeId,
            String postOfficeCode,
            List<String> postOfficeCodes,
            OrderType serviceType
    ) {
        return DashboardFilterRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .timezone(timezone)
                .granularity(granularity)
                .hubId(hubId)
                .postOfficeId(postOfficeId)
                .postOfficeCode(postOfficeCode)
                .postOfficeCodes(postOfficeCodes)
                .serviceType(serviceType)
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
