package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.ObjectiveScoreResponse;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.service.IRouteObjectiveScoringService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolBusAppConfigService;
import serp.project.school_bus_service.shared.code.AppConfigCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates a weighted objective value for a route planning solution.
 * Hard constraint violations are represented as large penalties, while soft
 * operational preferences such as distance and duration are weighted by
 * configurable parameters.
 */
@Service
public class RouteObjectiveScoringServiceImpl implements IRouteObjectiveScoringService {

    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRoutePlanningIssueService issueService;
    private final ISchoolBusAppConfigService appConfigService;
    private final IRoutePlanningSessionService sessionService;

    public RouteObjectiveScoringServiceImpl(
            IRouteService routeService,
            IRouteStopService routeStopService,
            IRoutePlanningIssueService issueService,
            ISchoolBusAppConfigService appConfigService,
            @Lazy IRoutePlanningSessionService sessionService) {
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.issueService = issueService;
        this.appConfigService = appConfigService;
        this.sessionService = sessionService;
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectiveScoreResponse calculateRouteScore(Long routePlanId, Long tenantId) {
        RoutePlanEntity route = routeService.getRouteEntity(routePlanId, tenantId);
        List<RouteStopEntity> stops = routeStopService.findByRoute(routePlanId, tenantId);
        List<RoutePlanningIssueEntity> issues = issueService.findByRoute(routePlanId);

        Map<String, BigDecimal> weights = loadWeights();
        return computeRouteScoreInternal(route, stops, issues, weights);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectiveScoreResponse calculateSolutionScore(Long sessionId, Long tenantId) {
        RoutePlanningSessionEntity session = sessionService.requireSession(sessionId, tenantId);
        List<RoutePlanEntity> routes = routeService.findRoutesBySession(sessionId, tenantId);

        Map<String, BigDecimal> weights = loadWeights();

        BigDecimal totalDistanceCost = BigDecimal.ZERO;
        BigDecimal totalDurationCost = BigDecimal.ZERO;
        BigDecimal totalWaitTimeCost = BigDecimal.ZERO;
        BigDecimal totalBlockingCost = BigDecimal.ZERO;
        BigDecimal totalWarningCost = BigDecimal.ZERO;
        BigDecimal totalCapacityExcessCost = BigDecimal.ZERO;

        List<Integer> studentCounts = new ArrayList<>();
        int totalBlockingIssues = 0;

        for (RoutePlanEntity route : routes) {
            List<RouteStopEntity> stops = routeStopService.findByRoute(route.getId(), tenantId);
            List<RoutePlanningIssueEntity> issues = issueService.findByRoute(route.getId());

            ObjectiveScoreResponse routeScore = computeRouteScoreInternal(route, stops, issues, weights);
            totalDistanceCost = totalDistanceCost.add(routeScore.getDistanceCost());
            totalDurationCost = totalDurationCost.add(routeScore.getDurationCost());
            totalWaitTimeCost = totalWaitTimeCost.add(routeScore.getWaitTimeCost());
            totalBlockingCost = totalBlockingCost.add(routeScore.getBlockingIssueCost());
            totalWarningCost = totalWarningCost.add(routeScore.getWarningIssueCost());
            totalCapacityExcessCost = totalCapacityExcessCost.add(routeScore.getCapacityExcessCost());

            studentCounts.add(route.getPlannedStudentCount() != null ? route.getPlannedStudentCount() : 0);
            totalBlockingIssues += route.getBlockingIssueCount() != null ? route.getBlockingIssueCount() : 0;
        }

        // Route Count Cost
        BigDecimal routeCountWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_ROUTE_COUNT);
        BigDecimal routeCountCost = routeCountWeight.multiply(BigDecimal.valueOf(routes.size()));

        // Unassigned Students Cost
        int unassignedCount = session.getTotalUnassignedStudents() != null ? session.getTotalUnassignedStudents() : 0;
        BigDecimal unassignedWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_UNASSIGNED);
        BigDecimal unassignedCost = unassignedWeight.multiply(BigDecimal.valueOf(unassignedCount));

        // Load Balance Cost
        BigDecimal balanceWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_LOAD_BALANCE);
        BigDecimal balanceCost = BigDecimal.ZERO;
        if (routes.size() > 1) {
            int maxStudents = studentCounts.stream().mapToInt(v -> v).max().orElse(0);
            int minStudents = studentCounts.stream().mapToInt(v -> v).min().orElse(0);
            int imbalance = maxStudents - minStudents;
            balanceCost = balanceWeight.multiply(BigDecimal.valueOf(imbalance));
        }

        BigDecimal objectiveValue = totalDistanceCost
                .add(totalDurationCost)
                .add(routeCountCost)
                .add(unassignedCost)
                .add(totalWaitTimeCost)
                .add(totalBlockingCost)
                .add(totalWarningCost)
                .add(totalCapacityExcessCost)
                .add(balanceCost);

        boolean feasible = totalBlockingIssues == 0 && unassignedCount == 0;
        BigDecimal displayScore = computeDisplayScore(objectiveValue);

        return ObjectiveScoreResponse.builder()
                .objectiveValue(objectiveValue.setScale(2, RoundingMode.HALF_UP))
                .displayScore(displayScore)
                .feasible(feasible)
                .distanceCost(totalDistanceCost.setScale(2, RoundingMode.HALF_UP))
                .durationCost(totalDurationCost.setScale(2, RoundingMode.HALF_UP))
                .routeCountCost(routeCountCost.setScale(2, RoundingMode.HALF_UP))
                .unassignedCost(unassignedCost.setScale(2, RoundingMode.HALF_UP))
                .waitTimeCost(totalWaitTimeCost.setScale(2, RoundingMode.HALF_UP))
                .blockingIssueCost(totalBlockingCost.setScale(2, RoundingMode.HALF_UP))
                .warningIssueCost(totalWarningCost.setScale(2, RoundingMode.HALF_UP))
                .capacityExcessCost(totalCapacityExcessCost.setScale(2, RoundingMode.HALF_UP))
                .balanceCost(balanceCost.setScale(2, RoundingMode.HALF_UP))
                .weights(weights)
                .build();
    }

    private ObjectiveScoreResponse computeRouteScoreInternal(
            RoutePlanEntity route,
            List<RouteStopEntity> stops,
            List<RoutePlanningIssueEntity> issues,
            Map<String, BigDecimal> weights) {

        // 1. Distance Cost
        double distance = route.getPlannedDistanceKm() != null ? route.getPlannedDistanceKm() : 0.0;
        BigDecimal distanceWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_DISTANCE);
        BigDecimal distanceCost = distanceWeight.multiply(BigDecimal.valueOf(distance));

        // 2. Duration Cost
        int duration = route.getPlannedDurationMin() != null ? route.getPlannedDurationMin() : 0;
        BigDecimal durationWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_DURATION);
        BigDecimal durationCost = durationWeight.multiply(BigDecimal.valueOf(duration));

        // 3. Wait Time / Travel Transit Time Cost
        double totalWaitTimeMin = calculateRouteWaitTime(route, stops);
        BigDecimal waitTimeWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME);
        BigDecimal waitTimeCost = waitTimeWeight.multiply(BigDecimal.valueOf(totalWaitTimeMin));

        // 4. Issue Penalties
        long blockingCount = issues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.BLOCKING).count();
        long warningCount = issues.stream().filter(i -> i.getSeverity() == PlanningIssueSeverity.WARNING).count();

        BigDecimal blockingWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_BLOCKING_ISSUE);
        BigDecimal warningWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_WARNING_ISSUE);

        BigDecimal blockingIssueCost = blockingWeight.multiply(BigDecimal.valueOf(blockingCount));
        BigDecimal warningIssueCost = warningWeight.multiply(BigDecimal.valueOf(warningCount));

        // 5. Capacity Excess Cost
        int studentCount = route.getPlannedStudentCount() != null ? route.getPlannedStudentCount() : 0;
        int capacity = route.getAssignedBusCapacity() != null ? route.getAssignedBusCapacity() : 30;
        int excess = Math.max(0, studentCount - capacity);
        BigDecimal capacityExcessWeight = weights.get(AppConfigCode.ROUTING_WEIGHT_CAPACITY_EXCESS);
        BigDecimal capacityExcessCost = capacityExcessWeight.multiply(BigDecimal.valueOf(excess));

        BigDecimal objectiveValue = distanceCost
                .add(durationCost)
                .add(waitTimeCost)
                .add(blockingIssueCost)
                .add(warningIssueCost)
                .add(capacityExcessCost);

        boolean feasible = blockingCount == 0 && excess == 0;
        BigDecimal displayScore = computeDisplayScore(objectiveValue);

        return ObjectiveScoreResponse.builder()
                .objectiveValue(objectiveValue.setScale(2, RoundingMode.HALF_UP))
                .displayScore(displayScore)
                .feasible(feasible)
                .distanceCost(distanceCost.setScale(2, RoundingMode.HALF_UP))
                .durationCost(durationCost.setScale(2, RoundingMode.HALF_UP))
                .routeCountCost(BigDecimal.ZERO)
                .unassignedCost(BigDecimal.ZERO)
                .waitTimeCost(waitTimeCost.setScale(2, RoundingMode.HALF_UP))
                .blockingIssueCost(blockingIssueCost.setScale(2, RoundingMode.HALF_UP))
                .warningIssueCost(warningIssueCost.setScale(2, RoundingMode.HALF_UP))
                .capacityExcessCost(capacityExcessCost.setScale(2, RoundingMode.HALF_UP))
                .balanceCost(BigDecimal.ZERO)
                .weights(weights)
                .build();
    }

    private double calculateRouteWaitTime(RoutePlanEntity route, List<RouteStopEntity> stops) {
        if (stops == null || stops.size() < 2) {
            return 0.0;
        }
        RouteStopEntity terminalStop = stops.stream()
                .filter(s -> s.getStopPurpose() != null && (route.getRouteDirection() == RouteDirection.OUTBOUND
                        ? s.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                        : s.getStopPurpose() == RouteStopPurpose.START_TERMINAL))
                .findFirst()
                .orElse(null);

        if (terminalStop == null || terminalStop.getPlannedArrivalTime() == null) {
            return 0.0;
        }

        LocalTime refTime = route.getRouteDirection() == RouteDirection.OUTBOUND
                ? terminalStop.getPlannedArrivalTime()
                : terminalStop.getPlannedDepartureTime();

        if (refTime == null) {
            return 0.0;
        }

        double totalTransitTime = 0.0;
        for (RouteStopEntity stop : stops) {
            if (stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal()) {
                continue;
            }
            int boardingCount = stop.getEstimatedStudentCount() != null ? stop.getEstimatedStudentCount() : 0;
            if (boardingCount <= 0) {
                continue;
            }

            LocalTime stopTime = route.getRouteDirection() == RouteDirection.OUTBOUND
                    ? stop.getPlannedDepartureTime()
                    : stop.getPlannedArrivalTime();

            if (stopTime != null) {
                long minutes = Math.abs(Duration.between(refTime, stopTime).toMinutes());
                totalTransitTime += boardingCount * minutes;
            }
        }
        return totalTransitTime;
    }

    private BigDecimal computeDisplayScore(BigDecimal objectiveValue) {
        double val = objectiveValue.doubleValue();
        double score = 100.0 / (1.0 + val / 500.0);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> loadWeights() {
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put(AppConfigCode.ROUTING_WEIGHT_DISTANCE, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_DISTANCE, BigDecimal.valueOf(1.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_DURATION, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_DURATION, BigDecimal.valueOf(1.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_ROUTE_COUNT, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_ROUTE_COUNT, BigDecimal.valueOf(10.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_UNASSIGNED, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_UNASSIGNED, BigDecimal.valueOf(1000.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_WAIT_TIME, BigDecimal.valueOf(0.5)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_BLOCKING_ISSUE, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_BLOCKING_ISSUE, BigDecimal.valueOf(10000.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_WARNING_ISSUE, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_WARNING_ISSUE, BigDecimal.valueOf(50.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_CAPACITY_EXCESS, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_CAPACITY_EXCESS, BigDecimal.valueOf(10000.0)));
        weights.put(AppConfigCode.ROUTING_WEIGHT_LOAD_BALANCE, appConfigService.getDecimal(AppConfigCode.ROUTING_WEIGHT_LOAD_BALANCE, BigDecimal.valueOf(2.0)));
        return weights;
    }
}
