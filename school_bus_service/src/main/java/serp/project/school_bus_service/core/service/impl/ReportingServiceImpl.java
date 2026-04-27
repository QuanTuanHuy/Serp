package serp.project.school_bus_service.core.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.application.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.CapacityUtilizationReportResponse;
import serp.project.school_bus_service.application.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.core.service.IReportingService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripExecutionEntity;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.infrastructure.store.repository.AuditLogRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TransportRequestRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripExecutionRepository;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements IReportingService {

    private final TransportRequestRepository transportRequestRepository;
    private final RoutePlanRepository routePlanRepository;
    private final TripExecutionRepository tripExecutionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final SchoolBusMapper mapper;

    @Override
    public OperationalReportResponse getOperationsSummary(ReportFilterParamsRequest params, Long tenantId) {
        long totalRequests = transportRequestRepository.findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .filter(request -> params == null || params.getSchoolId() == null
                        || request.getSchool().getId().equals(params.getSchoolId()))
                .count();
        long approvedRequests = transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId,
                RequestStatus.APPROVED);
        long rejectedRequests = transportRequestRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId,
                RequestStatus.REJECTED);
        long activeRoutes = routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId,
                RouteStatus.IN_PROGRESS);
        long completedRoutes = routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId,
                RouteStatus.COMPLETED);

        return new OperationalReportResponse(
                totalRequests,
                approvedRequests,
                rejectedRequests,
                activeRoutes,
                completedRoutes,
                attendanceRepository.countByTenantIdAndIsDeletedFalse(tenantId),
                auditLogRepository.countByTenantIdAndIsDeletedFalse(tenantId));
    }

    @Override
    public String exportOperationsSummaryCsv(ReportFilterParamsRequest params, Long tenantId) {
        OperationalReportResponse summary = getOperationsSummary(params, tenantId);
        return "metric,value\n"
                + "totalRequests," + summary.totalRequests() + "\n"
                + "approvedRequests," + summary.approvedRequests() + "\n"
                + "rejectedRequests," + summary.rejectedRequests() + "\n"
                + "activeRoutes," + summary.activeRoutes() + "\n"
                + "completedRoutes," + summary.completedRoutes() + "\n"
                + "attendanceEvents," + summary.attendanceEvents() + "\n"
                + "auditEvents," + summary.auditEvents() + "\n";
    }

    @Override
    public PageResponse<TripExecutionResponse> getTripsReport(ReportFilterParamsRequest params, Long tenantId) {
        return PageResponse.from(tripExecutionRepository.findAll(
                tripSpec(params, tenantId),
                pageable(params, Set.of("id", "tripCode", "serviceDate", "status", "createdAt"), "serviceDate")),
                trip -> mapper.toTripExecutionResponse(trip, null, null));
    }

    @Override
    public PageResponse<AttendanceResponse> getAttendanceReport(ReportFilterParamsRequest params, Long tenantId) {
        return PageResponse.from(attendanceRepository.findAll(
                attendanceSpec(params, tenantId),
                pageable(params, Set.of("id", "recordedAt", "createdAt"), "recordedAt")),
                mapper::toAttendanceResponse);
    }

    @Override
    public PageResponse<CapacityUtilizationReportResponse> getCapacityUtilization(ReportFilterParamsRequest params,
            Long tenantId) {
        return PageResponse.from(tripExecutionRepository.findAll(
                tripSpec(params, tenantId),
                pageable(params, Set.of("id", "tripCode", "serviceDate", "status", "createdAt"), "serviceDate")),
                trip -> {
                    int plannedStudents = trip.getRoute().getPlannedStudentCount() == null
                            ? 0
                            : trip.getRoute().getPlannedStudentCount();
                    Integer capacity = trip.getBus() == null ? trip.getRoute().getAssignedBusCapacity()
                            : trip.getBus().getCapacity();
                    int safeCapacity = capacity == null ? 0 : capacity;
                    double utilization = safeCapacity == 0 ? 0D : (plannedStudents * 100D / safeCapacity);
                    return new CapacityUtilizationReportResponse(
                            trip.getId(),
                            trip.getTripCode(),
                            trip.getRoute().getRouteCode(),
                            plannedStudents,
                            safeCapacity,
                            utilization);
                });
    }

    private Specification<TripExecutionEntity> tripSpec(ReportFilterParamsRequest params, Long tenantId) {
        Specification<TripExecutionEntity> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.isFalse(root.get("isDeleted")));
        if (params == null) {
            return spec;
        }
        if (params.getDateFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("serviceDate"), params.getDateFrom()));
        }
        if (params.getDateTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("serviceDate"), params.getDateTo()));
        }
        if (params.getSchoolId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("route", JoinType.INNER).get("school").get("id"),
                    params.getSchoolId()));
        }
        if (params.getRouteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("route").get("id"), params.getRouteId()));
        }
        if (params.getTripId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), params.getTripId()));
        }
        if (params.getDirection() != null) {
            RouteDirection direction = RouteDirection.valueOf(params.getDirection().toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("routeDirection"), direction));
        }
        if (params.getTripStatus() != null) {
            TripStatus status = TripStatus.valueOf(params.getTripStatus().toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return spec;
    }

    private Specification<AttendanceEntity> attendanceSpec(ReportFilterParamsRequest params, Long tenantId) {
        Specification<AttendanceEntity> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.isFalse(root.get("isDeleted")));
        if (params == null) {
            return spec;
        }
        if (params.getTripId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("trip").get("id"), params.getTripId()));
        }
        if (params.getRouteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("route").get("id"), params.getRouteId()));
        }
        if (params.getDateFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("recordedAt"),
                    params.getDateFrom().atStartOfDay()));
        }
        if (params.getDateTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("recordedAt"),
                    params.getDateTo().plusDays(1).atStartOfDay()));
        }
        return spec;
    }

    private Pageable pageable(ReportFilterParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}
