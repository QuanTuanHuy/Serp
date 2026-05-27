package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.service.IDashboardService;
import serp.project.school_bus_service.service.IParentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IStudentService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripHistoryService;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private final ISchoolService schoolService;
    private final IParentService parentService;
    private final IStudentService studentService;
    private final IBusService busService;
    private final ITransportRequestService transportRequestService;
    private final IRouteService routeService;
    private final ITripExecutionService tripExecutionService;
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
            ITripExecutionService tripExecutionService,
            ITripHistoryService tripHistoryService,
            IAttendanceService attendanceService,
            IAuditLogService auditLogService) {
        this.schoolService = schoolService;
        this.parentService = parentService;
        this.studentService = studentService;
        this.busService = busService;
        this.transportRequestService = transportRequestService;
        this.routeService = routeService;
        this.tripExecutionService = tripExecutionService;
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
                // Active trips = trips currently IN_PROGRESS (owned by TripExecution, not RoutePlan)
                tripExecutionService.countByTenantAndStatus(tenantId, TripStatus.IN_PROGRESS),
                tripHistoryService.countByTenant(tenantId));
    }

    @Override
    public OperationalReportResponse getOperationsReport(Long tenantId) {
        long totalRequests = transportRequestService.countByTenant(tenantId);
        long approvedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.APPROVED);
        long rejectedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.REJECTED);
        // Active/completed counts come from TripExecution, not RoutePlan
        long activeTrips = tripExecutionService.countByTenantAndStatus(tenantId, TripStatus.IN_PROGRESS);
        long completedTrips = tripExecutionService.countByTenantAndStatus(tenantId, TripStatus.COMPLETED);

        return new OperationalReportResponse(
                totalRequests,
                approvedRequests,
                rejectedRequests,
                activeTrips,
                completedTrips,
                attendanceService.countByTenant(tenantId),
                auditLogService.countByTenant(tenantId));
    }
}
