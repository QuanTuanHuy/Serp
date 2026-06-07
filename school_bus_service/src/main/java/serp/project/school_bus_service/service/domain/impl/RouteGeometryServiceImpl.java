package serp.project.school_bus_service.service.domain.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import serp.project.school_bus_service.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.dto.response.RoutePathLegInfo;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;
import serp.project.school_bus_service.service.domain.IRoutingEngineService;
import serp.project.school_bus_service.service.domain.ITimelineCalculatorService;
import tools.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Lazy;
import serp.project.school_bus_service.dto.request.RouteCalculationTraceCreateCommand;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.enums.RouteCalculationStatus;
import serp.project.school_bus_service.enums.RouteCalculationType;
import serp.project.school_bus_service.service.IRouteCalculationTraceService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates geometry computation for {@link RoutePlanEntity}.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Collect ordered waypoints from the route's start/end locations and active stops.</li>
 *   <li>Check if OSRM is enabled via config. If yes, call the primary (OSRM) engine.</li>
 *   <li>On OSRM failure or if OSRM is disabled, fall back to the straight-line engine.</li>
 *   <li>Trigger timeline calculations for route and stops.</li>
 *   <li>Mutate the route entity in-place; <b>caller must persist</b>.</li>
 * </ol>
 */
@Service
@Slf4j
public class RouteGeometryServiceImpl implements IRouteGeometryService {

    private final IRoutingEngineService primaryEngine;
    private final IRoutingEngineService fallbackEngine;
    private final ObjectMapper objectMapper;
    private final IRoutingConfigResolver routingConfigResolver;
    private final ITimelineCalculatorService timelineCalculatorService;
    private final IRouteCalculationTraceService traceService;
    private final IRoutePlanningIssueService issueService;

    public RouteGeometryServiceImpl(
            @Qualifier("osrmRoutingEngine") IRoutingEngineService primaryEngine,
            @Qualifier("straightLineRoutingEngine") IRoutingEngineService fallbackEngine,
            ObjectMapper objectMapper,
            IRoutingConfigResolver routingConfigResolver,
            ITimelineCalculatorService timelineCalculatorService,
            @Lazy IRouteCalculationTraceService traceService,
            @Lazy IRoutePlanningIssueService issueService) {
        this.primaryEngine = primaryEngine;
        this.fallbackEngine = fallbackEngine;
        this.objectMapper = objectMapper;
        this.routingConfigResolver = routingConfigResolver;
        this.timelineCalculatorService = timelineCalculatorService;
        this.traceService = traceService;
        this.issueService = issueService;
    }

    @Override
    public RoutePathResponse computeAndUpdate(RoutePlanEntity route, List<RouteStopEntity> stops) {
        List<RoutePathCoordinateResponse> waypoints = collectWaypoints(stops);
        RoutingRuntimeConfig config = routingConfigResolver.resolve();

        if (waypoints.size() < 2) {
            RoutePathResponse noGeom = noGeometryResponse(route.getId(), waypoints);
            route.setGeometryPath(serialize(noGeom));
            clearStopDistances(stops);
            saveTraceSafely(route, stops, noGeom, config, RouteCalculationStatus.PARTIAL);
            return noGeom;
        }

        Long tenantId = route.getTenantId();

        RoutePathResponse result;
        // OSRM is preferred for realistic road duration. If it is disabled or fails,
        // fallback keeps the planning demo operational by using haversine distance.
        if (config.isOsrmEnabled()) {
            try {
                result = primaryEngine.requestRoute(route.getId(), waypoints, tenantId);
            } catch (Exception ex) {
                log.warn("Primary routing engine failed for route {}, using straight-line fallback: {}",
                        route.getId(), ex.getMessage());
                result = fallbackEngine.requestRoute(route.getId(), waypoints, tenantId);
            }
        } else {
            log.info("OSRM is disabled by config, using straight-line fallback for route {}",
                    route.getId());
            result = fallbackEngine.requestRoute(route.getId(), waypoints, tenantId);
        }

        route.setGeometryPath(serialize(result));
        if (result.getDistanceKm() != null) {
            route.setPlannedDistanceKm(result.getDistanceKm());
        }
        if (result.getDurationMin() != null) {
            route.setPlannedDurationMin(result.getDurationMin());
        }
        applyLegDistancesToStops(stops, result);

        // Calculate stop-level planned arrival/departure times & route start/end times
        timelineCalculatorService.calculateTimeline(route, stops);

        saveTraceSafely(route, stops, result, config, RouteCalculationStatus.SUCCESS);

        return result;
    }

    /**
     * Persists a calculation snapshot for audit and demonstration purposes.
     * Unlike operational route issues, traces are not deleted on recalculation
     * because each trace explains one historical computation run.
     */
    private void saveTraceSafely(RoutePlanEntity route, List<RouteStopEntity> stops,
                                 RoutePathResponse result, RoutingRuntimeConfig config,
                                 RouteCalculationStatus status) {
        try {
            RouteCalculationTraceCreateCommand command = new RouteCalculationTraceCreateCommand();
            command.setRoutePlanId(route.getId());
            command.setPlanningSessionId(route.getPlanningSession() != null ? route.getPlanningSession().getId() : null);
            command.setTenantId(route.getTenantId());
            command.setCalculationType(RouteCalculationType.MATRIX_AND_TIMELINE);
            command.setCalculationStatus(status);
            command.setSourceSummary(result.getProvider());

            // 1. Build input_json
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("routePlanId", route.getId());
            inputMap.put("planningSessionId", route.getPlanningSession() != null ? route.getPlanningSession().getId() : null);
            inputMap.put("schoolId", route.getSchool() != null ? route.getSchool().getId() : null);
            inputMap.put("schoolScheduleId", route.getSchoolSchedule() != null ? route.getSchoolSchedule().getId() : null);
            inputMap.put("serviceDate", route.getServiceDate() != null ? route.getServiceDate().toString() : null);
            inputMap.put("direction", route.getRouteDirection() != null ? route.getRouteDirection().name() : null);
            inputMap.put("depotId", route.getStartDepot() != null ? route.getStartDepot().getId() : null);

            List<Map<String, Object>> stopsList = new ArrayList<>();
            for (RouteStopEntity stop : stops) {
                Map<String, Object> stopMap = new HashMap<>();
                stopMap.put("stopId", stop.getId());
                stopMap.put("pointKey", stop.getLocationType() + ":" + (stop.getPickupPoint() != null ? stop.getPickupPoint().getId() : ""));
                stopMap.put("pointType", stop.getLocationType().name());
                stopMap.put("pointName", stop.getDisplayName());
                stopMap.put("stopOrder", stop.getStopOrder());
                stopsList.add(stopMap);
            }
            inputMap.put("stopOrder", stopsList);
            command.setInputJson(objectMapper.writeValueAsString(inputMap));

            // 2. Build matrix_json
            Map<String, Object> matrixMap = new HashMap<>();
            List<Map<String, Object>> pointsList = new ArrayList<>();
            for (RouteStopEntity stop : stops) {
                Map<String, Object> pointMap = new HashMap<>();
                pointMap.put("pointKey", stop.getLocationType() + ":" + (stop.getPickupPoint() != null ? stop.getPickupPoint().getId() : ""));
                pointMap.put("pointType", stop.getLocationType().name());
                pointMap.put("name", stop.getDisplayName());
                pointMap.put("latitude", stop.getLatitude());
                pointMap.put("longitude", stop.getLongitude());
                pointsList.add(pointMap);
            }
            matrixMap.put("points", pointsList);
            matrixMap.put("durations", result.getLegs() != null ? result.getLegs().stream().map(RoutePathLegInfo::getDurationMin).toList() : List.of());
            matrixMap.put("distances", result.getLegs() != null ? result.getLegs().stream().map(RoutePathLegInfo::getDistanceKm).toList() : List.of());
            matrixMap.put("source", result.getProvider());
            matrixMap.put("usedFallback", result.getFallbackUsed() != null ? result.getFallbackUsed() : false);
            command.setMatrixJson(objectMapper.writeValueAsString(matrixMap));

            // 3. Build timeline_json
            Map<String, Object> timelineMap = new HashMap<>();
            timelineMap.put("direction", route.getRouteDirection() != null ? route.getRouteDirection().name() : null);
            timelineMap.put("routeStartTime", route.getPlannedStartTime() != null ? route.getPlannedStartTime().toString() : null);
            timelineMap.put("routeEndTime", route.getPlannedEndTime() != null ? route.getPlannedEndTime().toString() : null);

            List<Map<String, Object>> stopsTimeline = new ArrayList<>();
            for (RouteStopEntity stop : stops) {
                Map<String, Object> stopTime = new HashMap<>();
                stopTime.put("stopOrder", stop.getStopOrder());
                stopTime.put("pointKey", stop.getLocationType() + ":" + (stop.getPickupPoint() != null ? stop.getPickupPoint().getId() : ""));
                stopTime.put("plannedArrivalTime", stop.getPlannedArrivalTime() != null ? stop.getPlannedArrivalTime().toString() : null);
                stopTime.put("plannedDepartureTime", stop.getPlannedDepartureTime() != null ? stop.getPlannedDepartureTime().toString() : null);
                stopTime.put("distanceFromPreviousKm", stop.getDistanceFromPreviousKm());
                stopTime.put("travelFromPreviousMinutes", stop.getEstimatedTravelTimeFromPrevious() != null ? stop.getEstimatedTravelTimeFromPrevious() : 0);
                stopTime.put("dwellMinutes", config.getDwellTimeMinutes());
                stopsTimeline.add(stopTime);
            }
            timelineMap.put("stops", stopsTimeline);
            command.setTimelineJson(objectMapper.writeValueAsString(timelineMap));

            // 4. Build issues_json
            List<RoutePlanningIssueEntity> issues = issueService.findByRoute(route.getId());
            Map<String, Object> issuesMap = new HashMap<>();
            issuesMap.put("issueCount", route.getIssueCount() != null ? route.getIssueCount() : 0);
            issuesMap.put("blockingIssueCount", route.getBlockingIssueCount() != null ? route.getBlockingIssueCount() : 0);

            List<Map<String, Object>> issuesList = new ArrayList<>();
            if (issues != null) {
                for (RoutePlanningIssueEntity issue : issues) {
                    Map<String, Object> issueMap = new HashMap<>();
                    issueMap.put("code", issue.getIssueType());
                    issueMap.put("severity", issue.getSeverity() != null ? issue.getSeverity().name() : null);
                    issueMap.put("label", issue.getMessage());
                    issueMap.put("stopId", issue.getRouteStop() != null ? issue.getRouteStop().getId() : null);
                    issueMap.put("pointKey", issue.getRouteStop() != null ? issue.getRouteStop().getLocationType() + ":" + (issue.getRouteStop().getPickupPoint() != null ? issue.getRouteStop().getPickupPoint().getId() : "") : null);
                    issuesList.add(issueMap);
                }
            }
            issuesMap.put("issues", issuesList);
            command.setIssuesJson(objectMapper.writeValueAsString(issuesMap));

            // 5. Build config_snapshot_json
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("ROUTING_AVERAGE_SPEED_KMPH", String.valueOf(config.getAverageSpeedKmph()));
            configMap.put("ROUTING_DWELL_TIME_MINUTES", String.valueOf(config.getDwellTimeMinutes()));
            configMap.put("ROUTING_ROAD_FACTOR", String.valueOf(config.getRoadFactor()));
            configMap.put("ROUTING_OSRM_ENABLED", String.valueOf(config.isOsrmEnabled()));
            command.setConfigSnapshotJson(objectMapper.writeValueAsString(configMap));

            traceService.saveTrace(command);
        } catch (Exception ex) {
            log.warn("Failed to persist route calculation trace snapshot: {}", ex.getMessage(), ex);
            // TODO: Add retention policy for old calculation traces if needed.
        }
    }

    @Override
    public String serialize(RoutePathResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception ex) {
            log.warn("Unable to serialize RoutePathResponse", ex);
            return null;
        }
    }

    @Override
    public RoutePathResponse deserialize(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(rawJson, RoutePathResponse.class);
        } catch (Exception ex) {
            log.warn("Unable to deserialize RoutePathResponse from geometry_path", ex);
            return null;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Assigns distanceFromPreviousKm and estimatedTravelTimeFromPrevious to each stop from legs.
     *
     * <p>With terminal stops modelled as route stops, all waypoints come from stops in stopOrder.
     * Leg index: legs[i] = stops[i] → stops[i+1], so incoming leg for stop[i] = legs[i-1].
     * The first stop (order 0, START_TERMINAL) has no incoming leg.
     */
    private void applyLegDistancesToStops(List<RouteStopEntity> stops, RoutePathResponse result) {
        List<RoutePathLegInfo> legs = result.getLegs();
        if (legs == null || legs.isEmpty()) {
            clearStopDistances(stops);
            return;
        }
        List<RouteStopEntity> ordered = stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            int legIndex = i - 1; // incoming leg (no incoming leg for first stop)
            RouteStopEntity stop = ordered.get(i);
            if (legIndex >= 0 && legIndex < legs.size()) {
                RoutePathLegInfo leg = legs.get(legIndex);
                stop.setDistanceFromPreviousKm(leg.getDistanceKm());
                stop.setEstimatedTravelTimeFromPrevious(leg.getDurationMin());
            } else {
                stop.setDistanceFromPreviousKm(null);
                stop.setEstimatedTravelTimeFromPrevious(null);
            }
        }
    }

    private void clearStopDistances(List<RouteStopEntity> stops) {
        if (stops == null) return;
        stops.forEach(s -> {
            s.setDistanceFromPreviousKm(null);
            s.setEstimatedTravelTimeFromPrevious(null);
        });
    }

    /** Collects waypoints from all route stops ordered by stopOrder (terminals included). */
    private List<RoutePathCoordinateResponse> collectWaypoints(List<RouteStopEntity> stops) {
        List<RoutePathCoordinateResponse> waypoints = new ArrayList<>();
        stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .forEach(stop -> appendCoordinate(waypoints, stop.getLatitude(), stop.getLongitude()));
        return waypoints;
    }

    private void appendCoordinate(List<RoutePathCoordinateResponse> target, Double lat, Double lon) {
        if (lat != null && lon != null) {
            target.add(new RoutePathCoordinateResponse(lat, lon));
        }
    }

    private RoutePathResponse noGeometryResponse(Long routeId, List<RoutePathCoordinateResponse> waypoints) {
        RoutePathResponse r = new RoutePathResponse();
        r.setRouteId(routeId);
        r.setProvider("NONE");
        r.setEstimated(Boolean.TRUE);
        r.setFallbackUsed(Boolean.FALSE);
        r.setGeometrySource("NONE");
        r.setCoordinates(waypoints == null ? List.of() : waypoints);
        r.setWarning("Insufficient coordinates — add latitude/longitude to all route locations.");
        return r;
    }
}
