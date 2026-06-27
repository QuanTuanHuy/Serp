package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.params.ReportFilterParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.CapacityUtilizationReportResponse;
import serp.project.school_bus_service.dto.response.OperationalReportResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.service.IReportingService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.repository.AttendanceRepository;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.repository.projection.RouteAssignmentSummaryProjection;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportingServiceImpl implements IReportingService {

    private final ITransportRequestService transportRequestService;
    private final IRouteService routeService;
    private final TripExecutionRepository tripExecutionRepository;
    private final AttendanceRepository attendanceRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final IAttendanceService attendanceService;
    private final IRouteDispatchService routeDispatchService;
    private final SchoolBusMapper mapper;


    public ReportingServiceImpl(
            ITransportRequestService transportRequestService,
            IRouteService routeService,
            TripExecutionRepository tripExecutionRepository,
            AttendanceRepository attendanceRepository,
            RouteAssignmentRepository routeAssignmentRepository,
            IAttendanceService attendanceService,
            IRouteDispatchService routeDispatchService,
            SchoolBusMapper mapper) {
        this.transportRequestService = transportRequestService;
        this.routeService = routeService;
        this.tripExecutionRepository = tripExecutionRepository;
        this.attendanceRepository = attendanceRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.attendanceService = attendanceService;
        this.routeDispatchService = routeDispatchService;
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
                0L);
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
        var page = tripExecutionRepository.findReportTrips(
                tenantId,
                params == null ? null : params.getDateFrom(),
                params == null ? null : params.getDateTo(),
                params == null ? null : params.getSchoolId(),
                params == null ? null : params.getRouteId(),
                params == null ? null : params.getTripId(),
                parseDirection(params == null ? null : params.getDirection()),
                parseTripStatus(params == null ? null : params.getTripStatus()),
                pageable(params, Set.of("id", "tripCode", "status", "createdAt",
                        "route.planningSession.serviceDate",
                        "route.planningSession.routeDirection"), "route.planningSession.serviceDate"));
        Map<Long, RouteAssignmentSummaryProjection> assignments = assignmentSummariesByRoute(page.getContent(), tenantId);
        return PageResponse.from(page, trip -> {
            applyAssignmentSummary(trip, assignments.get(trip.getRoute().getId()));
            return mapper.toTripExecutionResponse(trip, null, null);
        });
    }

    @Override
    public PageResponse<AttendanceResponse> getAttendanceReport(ReportFilterParamsRequest params, Long tenantId) {
        LocalDateTime from = params == null || params.getDateFrom() == null
                ? null
                : params.getDateFrom().atStartOfDay();
        LocalDateTime to = params == null || params.getDateTo() == null
                ? null
                : params.getDateTo().plusDays(1).atStartOfDay();
        Long tripId = params == null ? null : params.getTripId();
        Long routeId = params == null ? null : params.getRouteId();
        Pageable pageable = attendancePageable(params);
        if (from != null && to != null) {
            return PageResponse.from(attendanceRepository.findReportAttendanceBetween(
                    tenantId, tripId, routeId, from, to, pageable), mapper::toAttendanceResponse);
        }
        if (from != null) {
            return PageResponse.from(attendanceRepository.findReportAttendanceFrom(
                    tenantId, tripId, routeId, from, pageable), mapper::toAttendanceResponse);
        }
        if (to != null) {
            return PageResponse.from(attendanceRepository.findReportAttendanceTo(
                    tenantId, tripId, routeId, to, pageable), mapper::toAttendanceResponse);
        }
        return PageResponse.from(attendanceRepository.findReportAttendance(
                tenantId, tripId, routeId, pageable), mapper::toAttendanceResponse);
    }

    @Override
    public PageResponse<CapacityUtilizationReportResponse> getCapacityUtilization(ReportFilterParamsRequest params,
            Long tenantId) {
        var page = tripExecutionRepository.findReportTrips(
                tenantId,
                params == null ? null : params.getDateFrom(),
                params == null ? null : params.getDateTo(),
                params == null ? null : params.getSchoolId(),
                params == null ? null : params.getRouteId(),
                params == null ? null : params.getTripId(),
                parseDirection(params == null ? null : params.getDirection()),
                parseTripStatus(params == null ? null : params.getTripStatus()),
                pageable(params, Set.of("id", "tripCode", "status", "createdAt",
                        "route.planningSession.serviceDate",
                        "route.planningSession.routeDirection"), "route.planningSession.serviceDate"));
        Map<Long, RouteAssignmentSummaryProjection> assignments = assignmentSummariesByRoute(page.getContent(), tenantId);
        return PageResponse.from(
                page,
                trip -> {
                    applyAssignmentSummary(trip, assignments.get(trip.getRoute().getId()));
                    int plannedStudents = trip.getRoute().getPlannedStudentCount() == null
                            ? 0
                            : trip.getRoute().getPlannedStudentCount();
                    Integer capacity = trip.getBus() == null ? null : trip.getBus().getCapacity();
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

    private RouteDirection parseDirection(String value) {
        return value == null || value.isBlank() ? null : RouteDirection.valueOf(value.toUpperCase());
    }

    private TripStatus parseTripStatus(String value) {
        return value == null || value.isBlank() ? null : TripStatus.valueOf(value.toUpperCase());
    }

    private void populateAssignment(TripExecutionEntity trip, Long tenantId) {
        if (trip == null || trip.getRoute() == null || trip.getRoute().getId() == null) {
            return;
        }
        routeDispatchService.findAssignmentEntityByRoute(trip.getRoute().getId(), tenantId)
                .ifPresent(assignment -> {
                    trip.setBus(assignment.getBus());
                    trip.setDriver(assignment.getDriver());
                    trip.setAttendant(assignment.getAttendant());
                });
    }

    private Map<Long, RouteAssignmentSummaryProjection> assignmentSummariesByRoute(List<TripExecutionEntity> trips,
                                                                                    Long tenantId) {
        List<Long> routeIds = trips.stream()
                .filter(trip -> trip.getRoute() != null)
                .map(trip -> trip.getRoute().getId())
                .distinct()
                .toList();
        if (routeIds.isEmpty()) {
            return Map.of();
        }
        return routeAssignmentRepository.findCurrentSummariesByRouteIds(tenantId, routeIds)
                .stream()
                .collect(Collectors.toMap(RouteAssignmentSummaryProjection::getRouteId, java.util.function.Function.identity()));
    }

    private void applyAssignmentSummary(TripExecutionEntity trip, RouteAssignmentSummaryProjection summary) {
        if (trip == null || summary == null) {
            return;
        }
        if (summary.getBusId() != null) {
            BusEntity bus = new BusEntity();
            bus.setId(summary.getBusId());
            bus.setPlateNumber(summary.getBusPlateNumber());
            bus.setCapacity(summary.getBusCapacity());
            bus.setStatus(summary.getBusStatus());
            trip.setBus(bus);
        }
        if (summary.getDriverId() != null) {
            DriverProfileEntity driver = new DriverProfileEntity();
            driver.setId(summary.getDriverId());
            driver.setFullName(summary.getDriverName());
            trip.setDriver(driver);
        }
        if (summary.getAttendantId() != null) {
            BusAttendantProfileEntity attendant = new BusAttendantProfileEntity();
            attendant.setId(summary.getAttendantId());
            attendant.setFullName(summary.getAttendantName());
            trip.setAttendant(attendant);
        }
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        BaseParamsRequest sortParams = params;
        Set<String> effectiveAllowedSorts = allowedSorts;
        String mappedSortBy = mapReportSort(params == null ? null : params.getSortBy());
        if (mappedSortBy != null) {
            sortParams = copyParamsWithSort(params, mappedSortBy);
            effectiveAllowedSorts = new HashSet<>(allowedSorts);
            effectiveAllowedSorts.add(mappedSortBy);
        }
        return PageableUtils.from(sortParams, effectiveAllowedSorts, defaultSortBy);
    }

    private Pageable attendancePageable(BaseParamsRequest params) {
        Set<String> allowedSorts = Set.of("id", "recordedAt", "createdAt");
        BaseParamsRequest sortParams = params;
        Set<String> effectiveAllowedSorts = allowedSorts;
        String mappedSortBy = mapAttendanceSort(params == null ? null : params.getSortBy());
        if (mappedSortBy != null) {
            sortParams = copyParamsWithSort(params, mappedSortBy);
            effectiveAllowedSorts = new HashSet<>(allowedSorts);
            effectiveAllowedSorts.add(mappedSortBy);
        }
        return PageableUtils.from(sortParams, effectiveAllowedSorts, "recordedAt");
    }

    private String mapReportSort(String sortBy) {
        if ("serviceDate".equals(sortBy)) {
            return "route.planningSession.serviceDate";
        }
        if ("routeDirection".equals(sortBy)) {
            return "route.planningSession.routeDirection";
        }
        return null;
    }

    private String mapAttendanceSort(String sortBy) {
        if ("serviceDate".equals(sortBy)) {
            return "tripStudent.trip.route.planningSession.serviceDate";
        }
        if ("routeDirection".equals(sortBy)) {
            return "tripStudent.trip.route.planningSession.routeDirection";
        }
        return null;
    }

    private BaseParamsRequest copyParamsWithSort(BaseParamsRequest source, String sortBy) {
        BaseParamsRequest copy = new BaseParamsRequest() {
        };
        if (source != null) {
            copy.setPage(source.getPage());
            copy.setSize(source.getSize());
            copy.setSortDirection(source.getSortDirection());
            copy.setKeyword(source.getKeyword());
        }
        copy.setSortBy(sortBy);
        return copy;
    }
}
