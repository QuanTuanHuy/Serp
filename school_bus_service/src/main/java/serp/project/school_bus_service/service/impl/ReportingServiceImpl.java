package serp.project.school_bus_service.service.impl;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.CapacityUtilizationReportResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.service.IReportingService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.repository.AttendanceRepository;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.util.Set;

@Service
public class ReportingServiceImpl implements IReportingService {

    private final ITransportRequestService transportRequestService;
    private final IRouteService routeService;
    private final TripExecutionRepository tripExecutionRepository;
    private final AttendanceRepository attendanceRepository;
    private final IAttendanceService attendanceService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;


    public ReportingServiceImpl(
            ITransportRequestService transportRequestService,
            IRouteService routeService,
            TripExecutionRepository tripExecutionRepository,
            AttendanceRepository attendanceRepository,
            IAttendanceService attendanceService,
            IAuditLogService auditLogService,
            SchoolBusMapper mapper) {
        this.transportRequestService = transportRequestService;
        this.routeService = routeService;
        this.tripExecutionRepository = tripExecutionRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceService = attendanceService;
        this.auditLogService = auditLogService;
        this.mapper = mapper;
    }


    @Override
    public OperationalReportResponse getOperationsSummary(ReportFilterParamsRequest params, Long tenantId) {
        long totalRequests;
        if (params != null && params.getSchoolId() != null) {
            totalRequests = transportRequestService.countBySchoolAndTenant(params.getSchoolId(), tenantId);
        } else {
            totalRequests = transportRequestService.countByTenant(tenantId);
        }
        long approvedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.APPROVED);
        long rejectedRequests = transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.REJECTED);
        // Active/completed counts come from TripExecution, not RoutePlan
        long activeTrips = tripExecutionRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, TripStatus.IN_PROGRESS);
        long completedTrips = tripExecutionRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, TripStatus.COMPLETED);

        return new OperationalReportResponse(
                totalRequests,
                approvedRequests,
                rejectedRequests,
                activeTrips,
                completedTrips,
                attendanceService.countByTenant(tenantId),
                auditLogService.countByTenant(tenantId));
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
