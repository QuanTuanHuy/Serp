package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.service.IDashboardService;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IStudentService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.service.ITripHistoryService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteStatus;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private final ISchoolService schoolService;
    private final IParentService parentService;
    private final IStudentService studentService;
    private final IBusService busService;
    private final ITransportRequestService transportRequestService;
    private final IRouteService routeService;
    private final ITripHistoryService tripHistoryService;
    private final IAttendanceService attendanceService;
    private final IAuditLogService auditLogService;

    public DashboardServiceImpl(
            ISchoolService schoolService,
            IParentService parentService,
            IStudentService studentService,
            IBusService busService,
            ITransportRequestService transportRequestService,
            IRouteService routeService,
            ITripHistoryService tripHistoryService,
            IAttendanceService attendanceService,
            IAuditLogService auditLogService) {
        this.schoolService = schoolService;
        this.parentService = parentService;
        this.studentService = studentService;
        this.busService = busService;
        this.transportRequestService = transportRequestService;
        this.routeService = routeService;
        this.tripHistoryService = tripHistoryService;
        this.attendanceService = attendanceService;
        this.auditLogService = auditLogService;
    }

    @Override
    public DashboardSummaryResponse getSummary(Long tenantId) {
        return new DashboardSummaryResponse(
                schoolService.countByTenant(tenantId),
                parentService.countByTenant(tenantId),
                studentService.countByTenant(tenantId),
                busService.countByTenant(tenantId),
                transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.SUBMITTED),
                routeService.countByTenantAndStatus(tenantId, RouteStatus.ASSIGNED),
                routeService.countByTenantAndStatus(tenantId, RouteStatus.IN_PROGRESS),
                tripHistoryService.countByTenant(tenantId));
    }

    @Override
    public OperationalReportResponse getOperationsReport(Long tenantId) {
        long totalRequests = transportRequestService.countByTenant(tenantId);
        long approvedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.APPROVED);
        long rejectedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.REJECTED);
        long activeRoutes = routeService.countByTenantAndStatus(tenantId, RouteStatus.IN_PROGRESS);
        long completedRoutes = routeService.countByTenantAndStatus(tenantId, RouteStatus.COMPLETED);

        return new OperationalReportResponse(
                totalRequests,
                approvedRequests,
                rejectedRequests,
                activeRoutes,
                completedRoutes,
                attendanceService.countByTenant(tenantId),
                auditLogService.countByTenant(tenantId));
    }
}
