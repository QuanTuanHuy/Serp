package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import serp.project.school_bus_service.dto.response.*;
import serp.project.school_bus_service.entity.*;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.repository.*;
import serp.project.school_bus_service.service.*;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;

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

    private final TripExecutionRepository tripExecutionRepository;
    private final TripStudentRepository tripStudentRepository;
    private final RoutePlanRepository routePlanRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final SchoolBusMapper schoolBusMapper;

    private final SchoolBusSecurityService schoolBusSecurityService;
    private final SchoolBusUserRepository schoolBusUserRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final StudentRepository studentRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final StudentSubscriptionRepository studentSubscriptionRepository;

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
            TripExecutionRepository tripExecutionRepository,
            TripStudentRepository tripStudentRepository,
            RoutePlanRepository routePlanRepository,
            RouteAssignmentRepository routeAssignmentRepository,
            TransportRequestRepository transportRequestRepository,
            AttendanceRepository attendanceRepository,
            SchoolBusMapper schoolBusMapper,
            SchoolBusSecurityService schoolBusSecurityService,
            SchoolBusUserRepository schoolBusUserRepository,
            ParentProfileRepository parentProfileRepository,
            StudentRepository studentRepository,
            DriverProfileRepository driverProfileRepository,
            BusAttendantProfileRepository busAttendantProfileRepository,
            StudentSubscriptionRepository studentSubscriptionRepository) {
        this.schoolService = schoolService;
        this.parentService = parentService;
        this.studentService = studentService;
        this.busService = busService;
        this.transportRequestService = transportRequestService;
        this.routeService = routeService;
        this.tripExecutionService = tripExecutionService;
        this.tripHistoryService = tripHistoryService;
        this.attendanceService = attendanceService;
        this.tripExecutionRepository = tripExecutionRepository;
        this.tripStudentRepository = tripStudentRepository;
        this.routePlanRepository = routePlanRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.attendanceRepository = attendanceRepository;
        this.schoolBusMapper = schoolBusMapper;
        this.schoolBusSecurityService = schoolBusSecurityService;
        this.schoolBusUserRepository = schoolBusUserRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.studentRepository = studentRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.busAttendantProfileRepository = busAttendantProfileRepository;
        this.studentSubscriptionRepository = studentSubscriptionRepository;
    }


    @Override
    public DashboardSummaryResponse getSummary(Long tenantId) {
        if (schoolBusSecurityService.isParent()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            ParentProfileEntity parent = parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (parent == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            List<StudentEntity> students = studentRepository.findByTenantIdAndParentProfileIdAndIsDeletedFalse(tenantId, parent.getId());
            long studentCount = students.size();
            long schoolCount = students.stream().map(s -> s.getSchool().getId()).distinct().count();
            long parentCount = 1;
            long busCount = 0;
            long pendingRequests = transportRequestRepository.countByTenantIdAndParentProfileIdAndStatusAndIsDeletedFalse(tenantId, parent.getId(), RequestStatus.SUBMITTED);
            
            long activeSubscriptions = 0;
            for (StudentEntity s : students) {
                activeSubscriptions += studentSubscriptionRepository.findByStudentIdAndTenantIdAndIsDeletedFalse(s.getId(), tenantId).stream()
                        .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                        .count();
            }
            
            return new DashboardSummaryResponse(
                    schoolCount,
                    parentCount,
                    studentCount,
                    busCount,
                    pendingRequests,
                    activeSubscriptions,
                    0,
                    0
            );
        } else if (schoolBusSecurityService.isDriver()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            DriverProfileEntity driver = driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (driver == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            List<TripExecutionEntity> trips = tripExecutionRepository.findTripsByDriverAndDate(tenantId, driver.getId(), LocalDate.now());
            long inProgressCount = trips.stream().filter(t -> t.getStatus() == TripStatus.IN_PROGRESS).count();
            long completedCount = trips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();
            return new DashboardSummaryResponse(
                    0, 0, 0, 0, 0, 0,
                    inProgressCount,
                    completedCount
            );
        } else if (schoolBusSecurityService.isAttendant()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            BusAttendantProfileEntity attendant = busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (attendant == null) {
                return new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0);
            }
            List<TripExecutionEntity> trips = tripExecutionRepository.findTripsByAttendantAndDate(tenantId, attendant.getId(), LocalDate.now());
            long inProgressCount = trips.stream().filter(t -> t.getStatus() == TripStatus.IN_PROGRESS).count();
            long completedCount = trips.stream().filter(t -> t.getStatus() == TripStatus.COMPLETED).count();
            return new DashboardSummaryResponse(
                    0, 0, 0, 0, 0, 0,
                    inProgressCount,
                    completedCount
            );
        }

        return new DashboardSummaryResponse(
                schoolService.countByTenant(tenantId),
                parentService.countByTenant(tenantId),
                studentService.countByTenant(tenantId),
                busService.countByTenant(tenantId),
                transportRequestService.countByTenantAndStatus(tenantId, RequestStatus.SUBMITTED),
                routeService.countByTenantAndStatus(tenantId, RouteStatus.ASSIGNED),
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
                0L);
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
            Optional<LocalDate> latestTripDate = tripExecutionRepository.findLatestServiceDate(tenantId);
            finalServiceDate = latestTripDate.orElseGet(LocalDate::now);
        }

        // 2. Fallback logic for fromDate & toDate
        LocalDate finalFromDate = fromDate != null ? fromDate : finalServiceDate.minusDays(6);
        LocalDate finalToDate = toDate != null ? toDate : finalServiceDate;

        // 3. Parse RouteDirection
        RouteDirection routeDir = (direction != null && !direction.isBlank()) ? RouteDirection.parse(direction) : null;

        // 13. Summary counters (Reuse existing summary logic)
        DashboardSummaryResponse summary = getSummary(tenantId);

        if (schoolBusSecurityService.isParent()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            ParentProfileEntity parent = parentProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (parent == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }

            // 4. Trip Status Distribution Chart for Parent's children
            List<Object[]> tripStatusRows = tripExecutionRepository.countTripsByStatusForParent(tenantId, parent.getId(), finalServiceDate);
            List<ChartItemDto> tripStatusChart = getChartItemsForTripStatus(tripStatusRows);

            // 5. Today Attendance Chart for Parent's children
            List<Object[]> attendanceRows = tripStudentRepository.countAttendanceByStatusForParent(tenantId, finalServiceDate, parent.getId());
            List<ChartItemDto> attendanceChart = getChartItemsForAttendanceStatus(attendanceRows);

            // 6. Route Readiness Chart (Unused, return empty)
            List<ChartItemDto> routeReadinessChart = List.of();

            // 7. Request Workload Chart for Parent (Count parent's own requests)
            List<TransportRequestEntity> parentRequests = transportRequestRepository.findByTenantIdAndParentProfileIdAndIsDeletedFalseOrderByCreatedAtDesc(tenantId, parent.getId());
            Map<RequestStatus, Long> parentRequestMap = parentRequests.stream()
                    .collect(Collectors.groupingBy(TransportRequestEntity::getStatus, Collectors.counting()));
            List<ChartItemDto> requestStatusChart = new ArrayList<>();
            for (RequestStatus status : RequestStatus.values()) {
                long count = parentRequestMap.getOrDefault(status, 0L);
                String label = switch (status) {
                    case DRAFT -> "Draft";
                    case SUBMITTED -> "Pending Approval";
                    case APPROVED -> "Approved";
                    case REJECTED -> "Rejected";
                    case CANCELLED -> "Cancelled";
                };
                requestStatusChart.add(new ChartItemDto(status.name(), count, label));
            }

            // 8. Trips by Date (Unused)
            List<ChartItemDto> tripsByDate = List.of();

            // 9. Direction Split (Unused)
            List<ChartItemDto> directionSplit = List.of();

            // 10. Active Routes for Parent (Limit to 5)
            List<RoutePlanEntity> routes = routePlanRepository.findRoutePlansByParentAndDate(tenantId, parent.getId(), finalServiceDate);
            List<RoutePlanResponse> activeRoutes = routes.stream()
                    .limit(5)
                    .map(schoolBusMapper::toRoutePlanResponse)
                    .toList();

            // 11. Pending Approval Queue (Limit to 5 requests of this parent)
            List<TransportRequestResponse> pendingApprovalQueue = parentRequests.stream()
                    .filter(r -> r.getStatus() == RequestStatus.SUBMITTED)
                    .limit(5)
                    .map(schoolBusMapper::toTransportRequestResponse)
                    .toList();

            // 12. Recent Attendance Activity for Parent's children (Limit to 10)
            List<AttendanceEntity> attendances = attendanceRepository.findRecentAttendanceForParent(tenantId, parent.getId(), PageRequest.of(0, 10));
            List<AttendanceResponse> recentAttendanceActivity = attendances.stream()
                    .map(schoolBusMapper::toAttendanceResponse)
                    .toList();

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
        } else if (schoolBusSecurityService.isDriver()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            DriverProfileEntity driver = driverProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (driver == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }

            // 4. Trip Status Distribution Chart for Driver
            List<Object[]> tripStatusRows = tripExecutionRepository.countTripsByStatusForDriver(tenantId, driver.getId(), finalServiceDate);
            List<ChartItemDto> tripStatusChart = getChartItemsForTripStatus(tripStatusRows);

            // 5. Today Attendance Chart for Driver's trips
            List<Object[]> attendanceRows = tripStudentRepository.countAttendanceByStatusForDriver(tenantId, finalServiceDate, driver.getId());
            List<ChartItemDto> attendanceChart = getChartItemsForAttendanceStatus(attendanceRows);

            // 6. Route Readiness Chart (Unused, return empty)
            List<ChartItemDto> routeReadinessChart = List.of();

            // 7. Request Workload (Unused)
            List<ChartItemDto> requestStatusChart = List.of();

            // 8. Trips by Date (Unused)
            List<ChartItemDto> tripsByDate = List.of();

            // 9. Direction Split (Unused)
            List<ChartItemDto> directionSplit = List.of();

            // 10. Active Routes for Driver (Limit to 5)
            List<RoutePlanEntity> routes = routePlanRepository.findRoutePlansByDriverAndDate(tenantId, driver.getId(), finalServiceDate);
            List<RoutePlanResponse> activeRoutes = routes.stream()
                    .limit(5)
                    .map(schoolBusMapper::toRoutePlanResponse)
                    .toList();

            // 11. Pending Approval Queue (Unused)
            List<TransportRequestResponse> pendingApprovalQueue = List.of();

            // 12. Recent Attendance Activity for Driver's trips (Limit to 10)
            List<AttendanceEntity> attendances = attendanceRepository.findRecentAttendanceForDriver(tenantId, driver.getId(), PageRequest.of(0, 10));
            List<AttendanceResponse> recentAttendanceActivity = attendances.stream()
                    .map(schoolBusMapper::toAttendanceResponse)
                    .toList();

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
        } else if (schoolBusSecurityService.isAttendant()) {
            String keycloakId = schoolBusSecurityService.getCurrentKeycloakId();
            SchoolBusUserEntity user = schoolBusUserRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId).orElse(null);
            if (user == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
            BusAttendantProfileEntity attendant = busAttendantProfileRepository.findByTenantIdAndUserIdAndIsDeletedFalse(tenantId, user.getId()).orElse(null);
            if (attendant == null) {
                return new DashboardOperationsResponse(summary, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }

            // 4. Trip Status Distribution Chart for Attendant
            List<Object[]> tripStatusRows = tripExecutionRepository.countTripsByStatusForAttendant(tenantId, attendant.getId(), finalServiceDate);
            List<ChartItemDto> tripStatusChart = getChartItemsForTripStatus(tripStatusRows);

            // 5. Today Attendance Chart for Attendant's trips
            List<Object[]> attendanceRows = tripStudentRepository.countAttendanceByStatusForAttendant(tenantId, finalServiceDate, attendant.getId());
            List<ChartItemDto> attendanceChart = getChartItemsForAttendanceStatus(attendanceRows);

            // 6. Route Readiness Chart (Unused, return empty)
            List<ChartItemDto> routeReadinessChart = List.of();

            // 7. Request Workload (Unused)
            List<ChartItemDto> requestStatusChart = List.of();

            // 8. Trips by Date (Unused)
            List<ChartItemDto> tripsByDate = List.of();

            // 9. Direction Split (Unused)
            List<ChartItemDto> directionSplit = List.of();

            // 10. Active Routes for Attendant (Limit to 5)
            List<RoutePlanEntity> routes = routePlanRepository.findRoutePlansByAttendantAndDate(tenantId, attendant.getId(), finalServiceDate);
            List<RoutePlanResponse> activeRoutes = routes.stream()
                    .limit(5)
                    .map(schoolBusMapper::toRoutePlanResponse)
                    .toList();

            // 11. Pending Approval Queue (Unused)
            List<TransportRequestResponse> pendingApprovalQueue = List.of();

            // 12. Recent Attendance Activity for Attendant's trips (Limit to 10)
            List<AttendanceEntity> attendances = attendanceRepository.findRecentAttendanceForAttendant(tenantId, attendant.getId(), PageRequest.of(0, 10));
            List<AttendanceResponse> recentAttendanceActivity = attendances.stream()
                    .map(schoolBusMapper::toAttendanceResponse)
                    .toList();

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

        // 4. Trip Status Distribution Chart
        List<Object[]> tripStatusRows = tripExecutionRepository.countTripsByStatusFiltered(tenantId, finalServiceDate, schoolId, routeDir);
        List<ChartItemDto> tripStatusChart = getChartItemsForTripStatus(tripStatusRows);

        // 5. Today Attendance Chart
        List<Object[]> attendanceRows = tripStudentRepository.countAttendanceByStatusFiltered(tenantId, finalServiceDate, schoolId, routeDir);
        List<ChartItemDto> attendanceChart = getChartItemsForAttendanceStatus(attendanceRows);

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
            row -> (Long) row[1],
            (v1, v2) -> v1
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

    private List<ChartItemDto> getChartItemsForTripStatus(List<Object[]> tripStatusRows) {
        List<ChartItemDto> tripStatusChart = new ArrayList<>();
        Map<String, Long> tripStatusMap = tripStatusRows.stream().collect(Collectors.toMap(
            row -> ((TripStatus) row[0]).name(),
            row -> (Long) row[1],
            (v1, v2) -> v1
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
        return tripStatusChart;
    }

    private List<ChartItemDto> getChartItemsForAttendanceStatus(List<Object[]> attendanceRows) {
        List<ChartItemDto> attendanceChart = new ArrayList<>();
        Map<String, Long> attendanceMap = attendanceRows.stream().collect(Collectors.toMap(
            row -> ((TripStudentStatus) row[0]).name(),
            row -> (Long) row[1],
            (v1, v2) -> v1
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
        return attendanceChart;
    }
}

