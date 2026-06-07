package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import serp.project.school_bus_service.dto.response.*;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.repository.*;
import serp.project.school_bus_service.service.*;
import serp.project.school_bus_service.mapper.SchoolBusMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final TripExecutionRepository tripExecutionRepository;
    private final TripStudentRepository tripStudentRepository;
    private final RoutePlanRepository routePlanRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final SchoolBusMapper schoolBusMapper;

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
            IAuditLogService auditLogService,
            TripExecutionRepository tripExecutionRepository,
            TripStudentRepository tripStudentRepository,
            RoutePlanRepository routePlanRepository,
            RouteAssignmentRepository routeAssignmentRepository,
            TransportRequestRepository transportRequestRepository,
            AttendanceRepository attendanceRepository,
            SchoolBusMapper schoolBusMapper) {
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
        this.tripExecutionRepository = tripExecutionRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.routePlanRepository = routePlanRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.attendanceRepository = attendanceRepository;
        this.schoolBusMapper = schoolBusMapper;
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

    @Override
    public DashboardOperationsResponse getOperationsDashboard(
            LocalDate serviceDate,
            LocalDate fromDate,
            LocalDate toDate,
            Long schoolId,
            String direction,
            Long tenantId) {

        // 1. Fallback logic for serviceDate
        LocalDate finalServiceDate = serviceDate;
        if (finalServiceDate == null) {
            // Fallback: Lấy latest serviceDate có trip của tenant này. Nếu không có, dùng LocalDate.now()
            Optional<LocalDate> latestTripDate = tripExecutionRepository.findLatestServiceDate(tenantId);
            if (latestTripDate.isPresent()) {
                finalServiceDate = latestTripDate.get();
            } else {
                finalServiceDate = LocalDate.now();
            }
        }

        // 2. Fallback logic for fromDate & toDate (Trips by Date)
        LocalDate finalFromDate = fromDate != null ? fromDate : finalServiceDate.minusDays(6);
        LocalDate finalToDate = toDate != null ? toDate : finalServiceDate;

        // 3. Parse RouteDirection
        RouteDirection routeDir = (direction != null && !direction.isBlank()) ? RouteDirection.parse(direction) : null;

        // 4. Trip Status Distribution Chart
        List<Object[]> tripStatusRows = tripExecutionRepository.countTripsByStatusFiltered(tenantId, finalServiceDate, schoolId, routeDir);
        List<ChartItemDto> tripStatusChart = new ArrayList<>();
        Map<String, Long> tripStatusMap = tripStatusRows.stream().collect(Collectors.toMap(
            row -> ((TripStatus) row[0]).name(),
            row -> (Long) row[1]
        ));
        for (TripStatus status : TripStatus.values()) {
            long count = tripStatusMap.getOrDefault(status.name(), 0L);
            String label = switch (status) {
                case PLANNED -> "Planned";
                case ASSIGNED -> "Assigned";
                case IN_PROGRESS -> "In Progress";
                case COMPLETED -> "Completed";
                case CANCELLED -> "Cancelled";
            };
            tripStatusChart.add(new ChartItemDto(status.name(), count, label));
        }

        // 5. Today Attendance Chart
        List<Object[]> attendanceRows = tripStudentRepository.countAttendanceByStatusFiltered(tenantId, finalServiceDate, schoolId, routeDir);
        List<ChartItemDto> attendanceChart = new ArrayList<>();
        Map<String, Long> attendanceMap = attendanceRows.stream().collect(Collectors.toMap(
            row -> ((TripStudentStatus) row[0]).name(),
            row -> (Long) row[1]
        ));
        for (TripStudentStatus status : TripStudentStatus.values()) {
            long count = attendanceMap.getOrDefault(status.name(), 0L);
            String label = switch (status) {
                case PLANNED -> "Planned";
                case BOARDED -> "Boarded";
                case DROPPED_OFF -> "Dropped Off";
                case ABSENT -> "Absent";
                case NO_SHOW -> "No Show";
                case NOT_SERVED -> "Not Served";
            };
            attendanceChart.add(new ChartItemDto(status.name(), count, label));
        }

        // 6. Route Readiness Chart (Calculated over operational routes of today)
        List<RoutePlanEntity> routes = routePlanRepository.findOperationalRoutes(tenantId, finalServiceDate, schoolId, routeDir);
        long readyCount = 0;
        long missingBusCount = 0;
        long missingDriverCount = 0;
        long missingAttendantCount = 0;

        for (RoutePlanEntity r : routes) {
            if (r.getStatus() == RouteStatus.TRIP_CREATED) {
                Optional<TripExecutionEntity> tripOpt = tripExecutionRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(r.getId(), tenantId);
                if (tripOpt.isPresent()) {
                    TripExecutionEntity trip = tripOpt.get();
                    boolean missingBus = trip.getBus() == null;
                    boolean missingDriver = trip.getDriver() == null;
                    boolean missingAttendant = trip.getAttendant() == null;

                    if (missingBus) missingBusCount++;
                    if (missingDriver) missingDriverCount++;
                    if (missingAttendant) missingAttendantCount++;
                    if (!missingBus && !missingDriver && !missingAttendant) {
                        readyCount++;
                    }
                } else {
                    missingBusCount++;
                    missingDriverCount++;
                    missingAttendantCount++;
                }
            } else {
                Optional<RouteAssignmentEntity> assignOpt = routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(r.getId(), tenantId);
                if (assignOpt.isPresent()) {
                    RouteAssignmentEntity assign = assignOpt.get();
                    boolean missingBus = assign.getBus() == null;
                    boolean missingDriver = assign.getDriver() == null;
                    boolean missingAttendant = assign.getAttendant() == null;

                    if (missingBus) missingBusCount++;
                    if (missingDriver) missingDriverCount++;
                    if (missingAttendant) missingAttendantCount++;
                    if (!missingBus && !missingDriver && !missingAttendant) {
                        readyCount++;
                    }
                } else {
                    missingBusCount++;
                    missingDriverCount++;
                    missingAttendantCount++;
                }
            }
        }
        List<ChartItemDto> routeReadinessChart = List.of(
            new ChartItemDto("Ready", readyCount, "Ready"),
            new ChartItemDto("Missing Bus", missingBusCount, "Missing Bus"),
            new ChartItemDto("Missing Driver", missingDriverCount, "Missing Driver"),
            new ChartItemDto("Missing Attendant", missingAttendantCount, "Missing Attendant")
        );

        // 7. Request Workload Chart
        List<Object[]> requestRows = transportRequestRepository.countRequestsByStatusFiltered(tenantId, schoolId);
        List<ChartItemDto> requestStatusChart = new ArrayList<>();
        Map<String, Long> requestMap = requestRows.stream().collect(Collectors.toMap(
            row -> ((RequestStatus) row[0]).name(),
            row -> (Long) row[1]
        ));
        for (RequestStatus status : RequestStatus.values()) {
            long count = requestMap.getOrDefault(status.name(), 0L);
            String label = switch (status) {
                case DRAFT -> "Draft";
                case SUBMITTED -> "Pending Approval";
                case APPROVED -> "Approved";
                case REJECTED -> "Rejected";
                case CANCELLED -> "Cancelled";
            };
            requestStatusChart.add(new ChartItemDto(status.name(), count, label));
        }

        // 8. Trips by Date
        List<Object[]> tripsByDateRows = tripExecutionRepository.countTripsByDateFiltered(tenantId, finalFromDate, finalToDate, schoolId, routeDir);
        List<ChartItemDto> tripsByDate = tripsByDateRows.stream()
            .map(row -> new ChartItemDto(row[0].toString(), (Long) row[1], row[0].toString()))
            .toList();

        // 9. Direction Split
        List<Object[]> directionSplitRows = tripExecutionRepository.countTripsByDirectionFiltered(tenantId, finalServiceDate, schoolId, routeDir);
        List<ChartItemDto> directionSplit = directionSplitRows.stream()
            .map(row -> {
                RouteDirection dir = (RouteDirection) row[0];
                String name = dir.name();
                String label = switch (dir) {
                    case OUTBOUND -> "Outbound";
                    case RETURN -> "Return";
                };
                return new ChartItemDto(name, (Long) row[1], label);
            })
            .toList();

        // 10. Active Routes (Limit to 5)
        List<RoutePlanResponse> activeRoutes = routes.stream()
            .limit(5)
            .map(schoolBusMapper::toRoutePlanResponse)
            .toList();

        // 11. Pending Approval Queue (Limit to 5)
        List<TransportRequestEntity> requests = transportRequestRepository.findRequestsFiltered(tenantId, schoolId);
        List<TransportRequestResponse> pendingApprovalQueue = requests.stream()
            .filter(r -> r.getStatus() == RequestStatus.SUBMITTED)
            .limit(5)
            .map(schoolBusMapper::toTransportRequestResponse)
            .toList();

        // 12. Recent Attendance Activity (Limit to 10)
        List<AttendanceEntity> attendances = attendanceRepository.findRecentAttendanceFiltered(tenantId, schoolId, PageRequest.of(0, 10));
        List<AttendanceResponse> recentAttendanceActivity = attendances.stream()
            .map(schoolBusMapper::toAttendanceResponse)
            .toList();

        // 13. Summary counters (Reuse existing summary logic)
        DashboardSummaryResponse summary = getSummary(tenantId);

        return new DashboardOperationsResponse(
            summary,
            tripStatusChart,
            attendanceChart,
            routeReadinessChart,
            requestStatusChart,
            tripsByDate,
            directionSplit,
            activeRoutes,
            pendingApprovalQueue,
            recentAttendanceActivity
        );
    }
}

