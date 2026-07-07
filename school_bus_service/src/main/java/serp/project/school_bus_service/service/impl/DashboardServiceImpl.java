/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.DashboardFilterParamsRequest;
import serp.project.school_bus_service.dto.response.ChartItemDto;
import serp.project.school_bus_service.dto.response.DashboardOperationsResponse;
import serp.project.school_bus_service.dto.response.DashboardSummaryResponse;
import serp.project.school_bus_service.dto.response.DropdownOptionResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.repository.BusRepository;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.repository.RoutePlanRepository;
import serp.project.school_bus_service.repository.SchoolRepository;
import serp.project.school_bus_service.repository.StudentRepository;
import serp.project.school_bus_service.repository.TransportRequestRepository;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.repository.TripStudentRepository;
import serp.project.school_bus_service.repository.projection.RouteReadinessCountProjection;
import serp.project.school_bus_service.service.IDashboardService;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.model.DashboardDataScope;
import serp.project.school_bus_service.service.model.DashboardQueryContext;
import serp.project.school_bus_service.service.model.DashboardResourceAssignment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private final ISchoolBusDataScopeService dataScopeService;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final BusRepository busRepository;
    private final TripExecutionRepository tripExecutionRepository;
    private final TripStudentRepository tripStudentRepository;
    private final RoutePlanRepository routePlanRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final TransportRequestRepository transportRequestRepository;

    public DashboardServiceImpl(
            ISchoolBusDataScopeService dataScopeService,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            BusRepository busRepository,
            TripExecutionRepository tripExecutionRepository,
            TripStudentRepository tripStudentRepository,
            RoutePlanRepository routePlanRepository,
            RouteAssignmentRepository routeAssignmentRepository,
            TransportRequestRepository transportRequestRepository) {
        this.dataScopeService = dataScopeService;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.busRepository = busRepository;
        this.tripExecutionRepository = tripExecutionRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.routePlanRepository = routePlanRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.transportRequestRepository = transportRequestRepository;
    }

    @Override
    public DashboardSummaryResponse getSummary(DashboardFilterParamsRequest params, Long tenantId) {
        return getSummary(buildContext(params, tenantId));
    }

    @Override
    public List<ChartItemDto> getTripStatusChart(DashboardFilterParamsRequest params, Long tenantId) {
        return getTripStatusChart(buildContext(params, tenantId));
    }

    @Override
    public List<ChartItemDto> getAttendanceStatusChart(DashboardFilterParamsRequest params, Long tenantId) {
        return getAttendanceStatusChart(buildContext(params, tenantId));
    }

    @Override
    public List<ChartItemDto> getRouteReadinessChart(DashboardFilterParamsRequest params, Long tenantId) {
        return getRouteReadinessChart(buildContext(params, tenantId));
    }

    @Override
    public List<ChartItemDto> getRequestStatusChart(DashboardFilterParamsRequest params, Long tenantId) {
        return getRequestStatusChart(buildContext(params, tenantId));
    }

    @Override
    public List<ChartItemDto> getTripsByDateChart(DashboardFilterParamsRequest params, Long tenantId) {
        return getTripsByDateChart(buildContext(params, tenantId));
    }

    @Override
    public List<DropdownOptionResponse> getDashboardSchools(Long tenantId) {
        DashboardDataScope scope = dataScopeService.getDashboardDataScope(tenantId);
        if (scope.getAllowedSchoolIds().isEmpty()) {
            return List.of();
        }

        return schoolRepository
                .findByTenantIdAndIdInAndIsActiveTrueAndIsDeletedFalseOrderByNameAsc(
                        tenantId,
                        scope.getAllowedSchoolIds())
                .stream()
                .map(this::toDropdownOption)
                .toList();
    }

    @Override
    @Deprecated
    public DashboardOperationsResponse getOperationsDashboard(
            LocalDate serviceDate,
            LocalDate fromDate,
            LocalDate toDate,
            Long schoolId,
            String direction,
            Long tenantId) {
        DashboardFilterParamsRequest params = new DashboardFilterParamsRequest();
        params.setServiceDate(serviceDate);
        params.setSchoolId(schoolId);
        params.setDirection(direction);

        DashboardQueryContext context = buildContext(params, tenantId);
        context.setFromDate(fromDate != null ? fromDate : context.getFromDate());
        context.setToDate(toDate != null ? toDate : context.getToDate());

        return new DashboardOperationsResponse(
                getSummary(context),
                getTripStatusChart(context),
                getAttendanceStatusChart(context),
                getRouteReadinessChart(context),
                getRequestStatusChart(context),
                getTripsByDateChart(context),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private DashboardSummaryResponse getSummary(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty()) {
            return emptySummary();
        }

        long schoolCount;
        long parentCount;
        long studentCount;

        if (scope.isTenantWide() || scope.getParentProfileId() != null) {
            schoolCount = context.getSchoolId() == null
                    ? scope.getAllowedSchoolIds().size()
                    : 1;
            parentCount = studentRepository.countDashboardParents(
                    scope.getTenantId(),
                    context.getSchoolId(),
                    scope.getParentProfileId());
            studentCount = studentRepository.countDashboardStudents(
                    scope.getTenantId(),
                    context.getSchoolId(),
                    scope.getParentProfileId());
        } else {
            schoolCount = tripExecutionRepository.countDashboardSchools(
                    scope.getTenantId(),
                    context.getServiceDate(),
                    context.getSchoolId(),
                    context.getDirection(),
                    scope.isTenantWide(),
                    scope.getDriverProfileId(),
                    scope.getAttendantProfileId(),
                    scope.getParentProfileId());
            parentCount = tripStudentRepository.countDashboardParents(
                    scope.getTenantId(),
                    context.getServiceDate(),
                    context.getSchoolId(),
                    context.getDirection(),
                    scope.isTenantWide(),
                    scope.getDriverProfileId(),
                    scope.getAttendantProfileId(),
                    scope.getParentProfileId());
            studentCount = tripStudentRepository.countDashboardStudents(
                    scope.getTenantId(),
                    context.getServiceDate(),
                    context.getSchoolId(),
                    context.getDirection(),
                    scope.isTenantWide(),
                    scope.getDriverProfileId(),
                    scope.getAttendantProfileId(),
                    scope.getParentProfileId());
        }

        long pendingRequestCount = supportsRequestMetrics(scope)
                ? transportRequestRepository.countDashboardRequests(
                        scope.getTenantId(),
                        context.getServiceDate(),
                        context.getSchoolId(),
                        scope.getParentProfileId(),
                        RequestStatus.SUBMITTED)
                : 0L;

        Map<TripStatus, Long> tripCounts = mapEnumCounts(
                getTripStatusRows(context),
                TripStatus.class);
        List<RoutePlanEntity> routes = getDashboardRoutes(context);
        long assignedRouteCount = routes.stream()
                .filter(route -> route.getStatus() == RouteStatus.ASSIGNED)
                .count();

        long busCount = scope.isTenantWide()
                ? busRepository.countByTenantIdAndIsDeletedFalse(scope.getTenantId())
                : 0L;

        return new DashboardSummaryResponse(
                schoolCount,
                parentCount,
                studentCount,
                busCount,
                pendingRequestCount,
                assignedRouteCount,
                tripCounts.getOrDefault(TripStatus.IN_PROGRESS, 0L),
                tripCounts.getOrDefault(TripStatus.COMPLETED, 0L));
    }

    private List<ChartItemDto> getTripStatusChart(DashboardQueryContext context) {
        if (context.getDataScope().isEmpty()) {
            return List.of();
        }
        Map<TripStatus, Long> counts = mapEnumCounts(getTripStatusRows(context), TripStatus.class);
        List<ChartItemDto> result = new ArrayList<>();
        for (TripStatus status : TripStatus.values()) {
            result.add(new ChartItemDto(status.name(), counts.getOrDefault(status, 0L), tripStatusLabel(status)));
        }
        return result;
    }

    private List<ChartItemDto> getAttendanceStatusChart(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty()) {
            return List.of();
        }

        List<Object[]> rows = tripStudentRepository.countDashboardAttendanceByStatus(
                scope.getTenantId(),
                context.getServiceDate(),
                context.getSchoolId(),
                context.getDirection(),
                scope.isTenantWide(),
                scope.getDriverProfileId(),
                scope.getAttendantProfileId(),
                scope.getParentProfileId());
        Map<TripStudentStatus, Long> counts = mapEnumCounts(rows, TripStudentStatus.class);

        List<ChartItemDto> result = new ArrayList<>();
        for (TripStudentStatus status : TripStudentStatus.values()) {
            result.add(new ChartItemDto(
                    status.name(),
                    counts.getOrDefault(status, 0L),
                    attendanceStatusLabel(status)));
        }
        return result;
    }

    private List<ChartItemDto> getRouteReadinessChart(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty() || scope.getParentProfileId() != null) {
            return List.of();
        }

        RouteReadinessCountProjection readiness = routePlanRepository.countDashboardRouteReadiness(
                scope.getTenantId(),
                context.getServiceDate(),
                context.getSchoolId(),
                context.getDirection() == null ? null : context.getDirection().name(),
                scope.isTenantWide(),
                scope.getDriverProfileId(),
                scope.getAttendantProfileId(),
                scope.getParentProfileId());

        return List.of(
                new ChartItemDto("Ready", safeLong(readiness.getReadyCount()), "Ready"),
                new ChartItemDto("Missing Bus", safeLong(readiness.getMissingBusCount()), "Missing Bus"),
                new ChartItemDto("Missing Driver", safeLong(readiness.getMissingDriverCount()), "Missing Driver"),
                new ChartItemDto("Missing Attendant", safeLong(readiness.getMissingAttendantCount()), "Missing Attendant"));
    }

    private List<ChartItemDto> getRequestStatusChart(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty() || !supportsRequestMetrics(scope)) {
            return List.of();
        }

        List<Object[]> rows = transportRequestRepository.countDashboardRequestsByStatus(
                scope.getTenantId(),
                context.getServiceDate(),
                context.getSchoolId(),
                scope.getParentProfileId());
        Map<RequestStatus, Long> counts = mapEnumCounts(rows, RequestStatus.class);

        List<ChartItemDto> result = new ArrayList<>();
        for (RequestStatus status : RequestStatus.values()) {
            result.add(new ChartItemDto(
                    status.name(),
                    counts.getOrDefault(status, 0L),
                    requestStatusLabel(status)));
        }
        return result;
    }

    private List<ChartItemDto> getTripsByDateChart(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty()) {
            return List.of();
        }

        return tripExecutionRepository.countDashboardTripsByDate(
                        scope.getTenantId(),
                        context.getFromDate(),
                        context.getToDate(),
                        context.getSchoolId(),
                        context.getDirection(),
                        scope.isTenantWide(),
                        scope.getDriverProfileId(),
                        scope.getAttendantProfileId(),
                        scope.getParentProfileId())
                .stream()
                .map(row -> {
                    String date = row[0].toString();
                    return new ChartItemDto(date, ((Number) row[1]).longValue(), date);
                })
                .toList();
    }

    private DashboardQueryContext buildContext(DashboardFilterParamsRequest params, Long tenantId) {
        DashboardFilterParamsRequest safeParams = params == null
                ? new DashboardFilterParamsRequest()
                : params;
        DashboardDataScope scope = dataScopeService.getDashboardDataScope(tenantId);
        dataScopeService.assertCanAccessDashboardSchool(scope, safeParams.getSchoolId());

        RouteDirection direction = RouteDirection.parseNullable(safeParams.getDirection());
        LocalDate serviceDate = safeParams.getServiceDate();
        if (serviceDate == null && !scope.isEmpty()) {
            serviceDate = tripExecutionRepository.findLatestDashboardServiceDate(
                            tenantId,
                            safeParams.getSchoolId(),
                            direction,
                            scope.isTenantWide(),
                            scope.getDriverProfileId(),
                            scope.getAttendantProfileId(),
                            scope.getParentProfileId())
                    .orElse(null);
        }
        if (serviceDate == null) {
            serviceDate = LocalDate.now();
        }

        DashboardQueryContext context = new DashboardQueryContext();
        context.setDataScope(scope);
        context.setServiceDate(serviceDate);
        context.setFromDate(serviceDate.minusDays(6));
        context.setToDate(serviceDate);
        context.setSchoolId(safeParams.getSchoolId());
        context.setDirection(direction);
        return context;
    }

    private List<Object[]> getTripStatusRows(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        return tripExecutionRepository.countDashboardTripsByStatus(
                scope.getTenantId(),
                context.getServiceDate(),
                context.getSchoolId(),
                context.getDirection(),
                scope.isTenantWide(),
                scope.getDriverProfileId(),
                scope.getAttendantProfileId(),
                scope.getParentProfileId());
    }

    private List<RoutePlanEntity> getDashboardRoutes(DashboardQueryContext context) {
        DashboardDataScope scope = context.getDataScope();
        if (scope.isEmpty()) {
            return List.of();
        }
        return routePlanRepository.findDashboardRoutes(
                scope.getTenantId(),
                context.getServiceDate(),
                context.getSchoolId(),
                context.getDirection(),
                scope.isTenantWide(),
                scope.getDriverProfileId(),
                scope.getAttendantProfileId(),
                scope.getParentProfileId());
    }

    private boolean supportsRequestMetrics(DashboardDataScope scope) {
        return scope.isTenantWide() || scope.getParentProfileId() != null;
    }

    private DashboardSummaryResponse emptySummary() {
        return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
    }

    private DropdownOptionResponse toDropdownOption(SchoolEntity school) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("latitude", school.getLatitude() != null ? school.getLatitude() : 0.0);
        metadata.put("longitude", school.getLongitude() != null ? school.getLongitude() : 0.0);

        return DropdownOptionResponse.builder()
                .id(school.getId())
                .label(school.getName())
                .code(school.getCode())
                .metadata(metadata)
                .build();
    }

    private <E extends Enum<E>> Map<E, Long> mapEnumCounts(List<Object[]> rows, Class<E> enumType) {
        Map<E, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            E key = enumType.cast(row[0]);
            counts.put(key, ((Number) row[1]).longValue());
        }
        return counts;
    }

    private RouteAssignmentEntity latestAssignment(
            RouteAssignmentEntity first,
            RouteAssignmentEntity second) {
        return first.getId() >= second.getId() ? first : second;
    }

    private TripExecutionEntity latestTrip(TripExecutionEntity first, TripExecutionEntity second) {
        return first.getId() >= second.getId() ? first : second;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private DashboardResourceAssignment resolveAssignment(
            RoutePlanEntity route,
            RouteAssignmentEntity assignment,
            TripExecutionEntity trip) {
        DashboardResourceAssignment resourceAssignment = new DashboardResourceAssignment();
        resourceAssignment.setBusMissing(assignment == null || assignment.getBus() == null);
        resourceAssignment.setDriverMissing(assignment == null || assignment.getDriver() == null);
        resourceAssignment.setAttendantMissing(assignment == null || assignment.getAttendant() == null);
        return resourceAssignment;
    }

    private String tripStatusLabel(TripStatus status) {
        return switch (status) {
            case PLANNED -> "Planned";
            case ASSIGNED -> "Assigned";
            case IN_PROGRESS -> "In Progress";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
        };
    }

    private String attendanceStatusLabel(TripStudentStatus status) {
        return switch (status) {
            case PLANNED -> "Planned";
            case BOARDED -> "Boarded";
            case DROPPED_OFF -> "Dropped Off";
            case ABSENT -> "Absent";
            case NO_SHOW -> "No Show";
            case NOT_SERVED -> "Not Served";
        };
    }

    private String requestStatusLabel(RequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Pending Approval";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
            case CANCELLED -> "Cancelled";
        };
    }
}
