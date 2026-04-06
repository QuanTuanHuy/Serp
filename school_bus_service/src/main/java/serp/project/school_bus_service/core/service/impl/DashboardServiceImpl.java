package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.application.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.application.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.core.service.IDashboardService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.infrastructure.store.repository.AuditLogRepository;
import serp.project.school_bus_service.infrastructure.store.repository.BusRepository;
import serp.project.school_bus_service.infrastructure.store.repository.ParentProfileRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.SchoolRepository;
import serp.project.school_bus_service.infrastructure.store.repository.StudentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TransportRequestRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripHistoryRepository;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final SchoolRepository schoolRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final StudentRepository studentRepository;
    private final BusRepository busRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final RoutePlanRepository routePlanRepository;
    private final TripHistoryRepository tripHistoryRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public DashboardSummaryResponse getSummary(Long tenantId) {
        return new DashboardSummaryResponse(
                schoolRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(tenantId).size(),
                parentProfileRepository.findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(tenantId).size(),
                studentRepository.findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(tenantId).size(),
                busRepository.findByTenantIdAndIsDeletedFalseOrderByPlateNumberAsc(tenantId).size(),
                transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RequestStatus.PENDING),
                routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RouteStatus.ASSIGNED),
                routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RouteStatus.IN_PROGRESS),
                tripHistoryRepository.countByTenantIdAndIsDeletedFalse(tenantId));
    }

    @Override
    public OperationalReportResponse getOperationsReport(Long tenantId) {
        long totalRequests = transportRequestRepository.findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId).size();
        long approvedRequests = transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RequestStatus.APPROVED);
        long rejectedRequests = transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RequestStatus.REJECTED);
        long activeRoutes = routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RouteStatus.IN_PROGRESS);
        long completedRoutes = routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, RouteStatus.COMPLETED);

        return new OperationalReportResponse(
                totalRequests,
                approvedRequests,
                rejectedRequests,
                activeRoutes,
                completedRoutes,
                attendanceRepository.countByTenantIdAndIsDeletedFalse(tenantId),
                auditLogRepository.countByTenantIdAndIsDeletedFalse(tenantId));
    }
}
