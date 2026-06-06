package serp.project.school_bus_service.service.domain.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointWindowEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.ISchoolPickupPointWindowService;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;
import serp.project.school_bus_service.service.domain.ITimelineCalculatorService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TimelineCalculatorServiceImpl implements ITimelineCalculatorService {

    private final IRoutingConfigResolver routingConfigResolver;
    private final ISchoolPickupPointWindowService windowService;
    private final IRoutePlanningIssueService issueService;
    private final IRoutePlanStudentService studentService;

    public TimelineCalculatorServiceImpl(
            IRoutingConfigResolver routingConfigResolver,
            @Lazy ISchoolPickupPointWindowService windowService,
            IRoutePlanningIssueService issueService,
            @Lazy IRoutePlanStudentService studentService) {
        this.routingConfigResolver = routingConfigResolver;
        this.windowService = windowService;
        this.issueService = issueService;
        this.studentService = studentService;
    }

    /**
     * Entry point to calculate the planned arrival and departure times for each stop
     * and validate the overall route against operational constraints.
     */
    @Override
    public void calculateTimeline(RoutePlanEntity route, List<RouteStopEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }

        // Configuration parameters are loaded globally
        RoutingRuntimeConfig config = routingConfigResolver.resolve();
        int dwellTimeMinutes = config.getDwellTimeMinutes();

        // Sort stops by stopOrder ascending to maintain travel chronology
        List<RouteStopEntity> orderedStops = stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .toList();

        RouteDirection direction = route.getRouteDirection();
        if (direction == RouteDirection.OUTBOUND) {
            calculateOutbound(route, orderedStops, dwellTimeMinutes);
        } else {
            calculateReturn(route, orderedStops, dwellTimeMinutes);
        }

        // Validate and generate issues
        validateAndGenerateIssues(route, orderedStops);
    }

    /**
     * Outbound routes are calculated backward from the school arrival deadline
     * so the final school stop is guaranteed to target the schedule deadline.
     */
    private void calculateOutbound(RoutePlanEntity route, List<RouteStopEntity> stops, int dwellTimeMinutes) {
        SchoolScheduleEntity schedule = route.getPlanningSession() != null ? route.getPlanningSession().getSchoolSchedule() : null;
        LocalTime arrivalDeadline = schedule != null && schedule.getArrivalDeadline() != null
                ? schedule.getArrivalDeadline()
                : LocalTime.of(8, 0); // safe fallback if schedule missing

        // Set for end terminal (last stop)
        RouteStopEntity lastStop = stops.get(stops.size() - 1);
        lastStop.setPlannedArrivalTime(arrivalDeadline);
        lastStop.setPlannedDepartureTime(arrivalDeadline);

        LocalTime currentTime = arrivalDeadline;

        // Iterate backwards from second to last stop down to first stop
        for (int i = stops.size() - 2; i >= 0; i--) {
            RouteStopEntity currentStop = stops.get(i);
            RouteStopEntity nextStop = stops.get(i + 1);

            Integer travelTime = nextStop.getEstimatedTravelTimeFromPrevious();
            if (travelTime == null) {
                travelTime = 0;
            }

            // Departure time from currentStop = arrivalTime at nextStop - travelTime
            LocalTime departureTime = currentTime.minusMinutes(travelTime);
            currentStop.setPlannedDepartureTime(departureTime);

            // Dwell time for currentStop
            int dwell = getDwellTime(currentStop, dwellTimeMinutes);
            LocalTime arrivalTime = departureTime.minusMinutes(dwell);
            currentStop.setPlannedArrivalTime(arrivalTime);

            currentTime = arrivalTime;
        }

        // Set route start and end times
        route.setPlannedStartTime(stops.get(0).getPlannedDepartureTime());
        route.setPlannedEndTime(arrivalDeadline);
    }

    /**
     * Return routes are calculated forward from the school departure time.
     */
    private void calculateReturn(RoutePlanEntity route, List<RouteStopEntity> stops, int dwellTimeMinutes) {
        SchoolScheduleEntity schedule = route.getPlanningSession() != null ? route.getPlanningSession().getSchoolSchedule() : null;
        LocalTime departureTime = schedule != null && schedule.getDepartureTime() != null
                ? schedule.getDepartureTime()
                : LocalTime.of(16, 0); // safe fallback if schedule missing

        // Set for start terminal (first stop)
        RouteStopEntity firstStop = stops.get(0);
        firstStop.setPlannedArrivalTime(departureTime);
        firstStop.setPlannedDepartureTime(departureTime);

        LocalTime currentTime = departureTime;

        // Iterate forwards from second stop to last stop
        for (int i = 1; i < stops.size(); i++) {
            RouteStopEntity currentStop = stops.get(i);

            Integer travelTime = currentStop.getEstimatedTravelTimeFromPrevious();
            if (travelTime == null) {
                travelTime = 0;
            }

            // Arrival time at currentStop = departureTime of previousStop + travelTime
            LocalTime arrivalTime = currentTime.plusMinutes(travelTime);
            currentStop.setPlannedArrivalTime(arrivalTime);

            // Dwell time for currentStop
            int dwell = getDwellTime(currentStop, dwellTimeMinutes);
            LocalTime departureTimeAtStop = arrivalTime.plusMinutes(dwell);
            currentStop.setPlannedDepartureTime(departureTimeAtStop);

            currentTime = departureTimeAtStop;
        }

        // Set route start and end times
        route.setPlannedStartTime(departureTime);
        route.setPlannedEndTime(stops.get(stops.size() - 1).getPlannedArrivalTime());
    }

    private int getDwellTime(RouteStopEntity stop, int configuredDwellTime) {
        RouteStopPurpose purpose = stop.getStopPurpose();
        if (purpose != null && purpose.isTerminal()) {
            return 0; // DEPOT or SCHOOL
        }
        return configuredDwellTime;
    }

    /**
     * Validates the route stops against various constraints such as coordinate presence,
     * travel matrix cells, time windows, and school arrival deadlines.
     */
    private void validateAndGenerateIssues(RoutePlanEntity route, List<RouteStopEntity> stops) {
        List<RoutePlanningIssueEntity> newIssues = new ArrayList<>();
        Long tenantId = route.getTenantId();

        // Check if fallback was used in geometry path
        if (route.getGeometryPath() != null && (route.getGeometryPath().contains("\"fallbackUsed\":true") || route.getGeometryPath().contains("\"fallback\":true"))) {
            newIssues.add(buildIssue(route, null, "OSRM_FALLBACK_USED", PlanningIssueSeverity.INFO,
                    "OSRM was unavailable or disabled; fallback straight-line estimate was used for route geometry."));
        }

        // 1. Stop sequence count validation (BLOCKING)
        if (stops == null || stops.size() < 2) {
            newIssues.add(buildIssue(route, null, "ROUTE_STOP_SEQUENCE_TOO_SHORT", PlanningIssueSeverity.BLOCKING,
                    "Route must contain at least 2 stops: a start terminal and an end terminal."));
        } else {
            // 2. Terminal validation (BLOCKING)
            boolean hasStartTerminal = stops.stream().anyMatch(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL);
            boolean hasEndTerminal = stops.stream().anyMatch(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL);

            if (!hasStartTerminal) {
                newIssues.add(buildIssue(route, null, "MISSING_START_TERMINAL", PlanningIssueSeverity.BLOCKING,
                        "Route has no start terminal."));
            }
            if (!hasEndTerminal) {
                newIssues.add(buildIssue(route, null, "MISSING_END_TERMINAL", PlanningIssueSeverity.BLOCKING,
                        "Route has no end terminal."));
            }

            RouteStopEntity firstStop = stops.get(0);
            RouteStopEntity lastStop = stops.get(stops.size() - 1);

            if (hasStartTerminal && firstStop.getStopPurpose() != RouteStopPurpose.START_TERMINAL) {
                newIssues.add(buildIssue(route, firstStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                        "The first stop of the route must be the start terminal."));
            }
            if (hasEndTerminal && lastStop.getStopPurpose() != RouteStopPurpose.END_TERMINAL) {
                newIssues.add(buildIssue(route, lastStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                        "The last stop of the route must be the end terminal."));
            }

            if (route.getRouteDirection() == RouteDirection.OUTBOUND) {
                if (firstStop.getLocationType() != RouteLocationType.DEPOT) {
                    newIssues.add(buildIssue(route, firstStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                            "Outbound route must start at a depot terminal."));
                }
                if (lastStop.getLocationType() != RouteLocationType.SCHOOL) {
                    newIssues.add(buildIssue(route, lastStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                            "Outbound route must end at a school terminal."));
                }
            } else if (route.getRouteDirection() == RouteDirection.RETURN) {
                if (firstStop.getLocationType() != RouteLocationType.SCHOOL) {
                    newIssues.add(buildIssue(route, firstStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                            "Return route must start at a school terminal."));
                }
                if (lastStop.getLocationType() != RouteLocationType.DEPOT) {
                    newIssues.add(buildIssue(route, lastStop, "INVALID_ROUTE_TERMINAL", PlanningIssueSeverity.BLOCKING,
                            "Return route must end at a depot terminal."));
                }
            }
        }

        // Pre-fetch other assigned students of session once to avoid N+1 queries
        List<Long> otherAssignedStudentIds = new ArrayList<>();
        if (route.getPlanningSession() != null) {
            otherAssignedStudentIds = studentService.findStudentsInOtherRoutesOfSession(
                    route.getPlanningSession().getId(), route.getId())
                    .stream()
                    .map(ps -> ps.getStudent().getId())
                    .toList();
        }

        // Track student IDs assigned to middle stops in this route to check local duplicate assignment
        java.util.Set<Long> localAssignedStudentIds = new java.util.HashSet<>();

        if (stops != null) {
            for (RouteStopEntity stop : stops) {
                // 3. Missing Coordinates (BLOCKING)
                if (stop.getLatitude() == null || stop.getLongitude() == null) {
                    newIssues.add(buildIssue(route, stop, "MISSING_COORDINATES", PlanningIssueSeverity.BLOCKING,
                            "Stop location '" + stop.getDisplayName() + "' has missing coordinates."));
                }

                // 4. Missing Matrix Cell (BLOCKING)
                if (stop.getStopOrder() > 0 && (stop.getDistanceFromPreviousKm() == null || stop.getEstimatedTravelTimeFromPrevious() == null)) {
                    newIssues.add(buildIssue(route, stop, "MATRIX_CELL_MISSING", PlanningIssueSeverity.BLOCKING,
                            "Distance/travel time information from previous stop is missing for '" + stop.getDisplayName() + "'."));
                }

                // 5. Missing Time Window & Time Window Late (BLOCKING)
                if (stop.getLocationType() == RouteLocationType.PICKUP_POINT && stop.getPickupPoint() != null) {
                    String directionKey = route.getRouteDirection() == RouteDirection.OUTBOUND ? "PICKUP_TO_SCHOOL" : "DROPOFF_FROM_SCHOOL";
                    Optional<SchoolPickupPointWindowEntity> windowOpt = windowService.findWindow(
                            route.getSchool().getId(),
                            stop.getPickupPoint().getId(),
                            route.getSchoolSchedule().getId(),
                            directionKey,
                            tenantId
                    );

                    if (windowOpt.isEmpty()) {
                        newIssues.add(buildIssue(route, stop, "MISSING_TIME_WINDOW", PlanningIssueSeverity.BLOCKING,
                                "Missing pickup/drop-off time window. No time window is configured for the stop according to the route direction."));
                    } else {
                        SchoolPickupPointWindowEntity window = windowOpt.get();
                        if (stop.getPlannedArrivalTime() != null && stop.getPlannedArrivalTime().isAfter(window.getWindowEnd())) {
                            newIssues.add(buildIssue(route, stop, "TIME_WINDOW_LATE", PlanningIssueSeverity.BLOCKING,
                                    "Stop '" + stop.getDisplayName() + "' arrival time (" + stop.getPlannedArrivalTime() + ") exceeds the window end time (" + window.getWindowEnd() + ")."));
                        }
                    }

                    // 6. Direction compatibility validation (BLOCKING)
                    String usageType = stop.getPickupPoint().getUsageType();
                    if (route.getRouteDirection() == RouteDirection.OUTBOUND && "DROPOFF".equalsIgnoreCase(usageType)) {
                        newIssues.add(buildIssue(route, stop, "STOP_DIRECTION_NOT_COMPATIBLE", PlanningIssueSeverity.BLOCKING,
                                "Stop '" + stop.getDisplayName() + "' is drop-off only but used in outbound route."));
                    } else if (route.getRouteDirection() == RouteDirection.RETURN && "PICKUP".equalsIgnoreCase(usageType)) {
                        newIssues.add(buildIssue(route, stop, "STOP_DIRECTION_NOT_COMPATIBLE", PlanningIssueSeverity.BLOCKING,
                                "Stop '" + stop.getDisplayName() + "' is pickup only but used in return route."));
                    }

                    // 7. Duplicate student validation (BLOCKING)
                    List<RoutePlanStudentEntity> stopStudents = studentService.findByRouteStop(stop.getId());
                    for (RoutePlanStudentEntity ps : stopStudents) {
                        Long studentId = ps.getStudent().getId();
                        if (stop.getStopPurpose() == RouteStopPurpose.PICKUP || stop.getStopPurpose() == RouteStopPurpose.DROPOFF) {
                            if (localAssignedStudentIds.contains(studentId)) {
                                newIssues.add(buildIssue(route, stop, "STUDENT_ALREADY_ASSIGNED_TO_ROUTE", PlanningIssueSeverity.BLOCKING,
                                        "Student '" + ps.getStudent().getFullName() + "' is assigned to multiple stops within this route."));
                            } else {
                                localAssignedStudentIds.add(studentId);
                            }
                        }

                        if (otherAssignedStudentIds.contains(studentId)) {
                            newIssues.add(buildIssue(route, stop, "STUDENT_ALREADY_ASSIGNED_TO_ROUTE", PlanningIssueSeverity.BLOCKING,
                                    "Student '" + ps.getStudent().getFullName() + "' is already assigned to another route in this session."));
                        }
                    }
                }

                // 8. Outbound School Arrival Deadline Missed (BLOCKING)
                if (route.getRouteDirection() == RouteDirection.OUTBOUND && stop.getStopPurpose() == RouteStopPurpose.END_TERMINAL && stop.getPlannedArrivalTime() != null) {
                    LocalTime deadline = route.getSchoolSchedule() != null ? route.getSchoolSchedule().getArrivalDeadline() : null;
                    if (deadline != null && stop.getPlannedArrivalTime().isAfter(deadline)) {
                        newIssues.add(buildIssue(route, stop, "SCHOOL_ARRIVAL_DEADLINE_MISSED", PlanningIssueSeverity.BLOCKING,
                                "Outbound arrival at school (" + stop.getPlannedArrivalTime() + ") missed the arrival deadline (" + deadline + ")."));
                    }
                }
            }
        }

        // 9. Capacity Validation (BLOCKING / WARNING)
        long studentCount = studentService.countDistinctStudentsByRoute(route.getId());
        route.setPlannedStudentCount((int) studentCount);
        if (route.getAssignedBusCapacity() == null) {
            newIssues.add(buildIssue(route, null, "BUS_NOT_ASSIGNED_CAPACITY_UNKNOWN", PlanningIssueSeverity.WARNING,
                    "No bus is assigned to this route yet; capacity constraints cannot be verified."));
        } else {
            if (studentCount > route.getAssignedBusCapacity()) {
                newIssues.add(buildIssue(route, null, "ROUTE_CAPACITY_EXCEEDED", PlanningIssueSeverity.BLOCKING,
                        "Route planned students (" + studentCount + ") exceeds the assigned bus capacity (" + route.getAssignedBusCapacity() + ")."));
            }
        }

        // Clean old issues for this route to prevent duplicate issue logs when recalculating multiple times
        List<RoutePlanningIssueEntity> oldIssues = issueService.findByRoute(route.getId());
        if (oldIssues != null && !oldIssues.isEmpty()) {
            for (RoutePlanningIssueEntity issue : oldIssues) {
                issue.setIsDeleted(true);
                issue.setIsActive(false);
            }
            issueService.saveAll(oldIssues);
        }

        // Save new issues
        if (!newIssues.isEmpty()) {
            issueService.saveAll(newIssues);
        }

        // Update route counters
        int blockingCount = (int) newIssues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.BLOCKING).count();
        route.setIssueCount(newIssues.size());
        route.setBlockingIssueCount(blockingCount);

        // TODO Phase 6: qualityScore/objectiveScore will be calculated by the formal
        // weighted objective function. Phase 3 only calculates timing and feasibility issues.
    }

    private RoutePlanningIssueEntity buildIssue(RoutePlanEntity route, RouteStopEntity stop,
                                                 String type, PlanningIssueSeverity severity, String message) {
        RoutePlanningIssueEntity issue = new RoutePlanningIssueEntity();
        issue.markCreated(route.getTenantId(), "SYSTEM");
        issue.setPlanningSession(route.getPlanningSession());
        issue.setRoute(route);
        issue.setRouteStop(stop);
        issue.setIssueType(type);
        issue.setSeverity(severity);
        issue.setMessage(message);
        issue.setIsResolved(Boolean.FALSE);
        return issue;
    }
}
