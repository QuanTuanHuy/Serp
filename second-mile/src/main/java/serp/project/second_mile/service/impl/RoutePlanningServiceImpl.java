/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.dto.request.InternalRoutePlanRequest;
import serp.project.second_mile.dto.response.InternalRoutePlanResponse;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.AuthUtils;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.service.RoutePlanningService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutePlanningServiceImpl implements RoutePlanningService {
    private static final String HUB_PREFIX = "HUB:";
    private static final String POST_OFFICE_PREFIX = "PO:";

    private final RouteRepository routeRepository;
    private final AuthUtils authUtils;

    @Override
    public InternalRoutePlanResponse planOrderRoute(InternalRoutePlanRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        String originPostOfficeCode = normalizePostOfficeCode(request == null
                ? null
                : request.getOriginPostOfficeCode());
        String destinationPostOfficeCode = normalizePostOfficeCode(request == null
                ? null
                : request.getDestinationPostOfficeCode());
        if (originPostOfficeCode == null || destinationPostOfficeCode == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (originPostOfficeCode.equals(destinationPostOfficeCode)) {
            return new InternalRoutePlanResponse(
                    originPostOfficeCode,
                    destinationPostOfficeCode,
                    true,
                    0.0,
                    0,
                    List.of()
            );
        }

        Map<String, List<Edge>> graph = buildGraph(tenantId);
        String source = postOfficeNode(originPostOfficeCode);
        String target = postOfficeNode(destinationPostOfficeCode);
        List<Route> path = findShortestPath(graph, source, target);
        if (path.isEmpty()) {
            return new InternalRoutePlanResponse(
                    originPostOfficeCode,
                    destinationPostOfficeCode,
                    false,
                    null,
                    null,
                    List.of()
            );
        }

        return toResponse(originPostOfficeCode, destinationPostOfficeCode, path);
    }

    private Map<String, List<Edge>> buildGraph(Long tenantId) {
        Map<String, List<Edge>> graph = new HashMap<>();
        for (Route route : routeRepository.findByTenantIdAndStatus(tenantId, RouteStatus.ACTIVE)) {
            String origin = resolveOriginNode(route);
            String destination = resolveDestinationNode(route);
            if (origin == null || destination == null) {
                continue;
            }
            graph.computeIfAbsent(origin, ignored -> new ArrayList<>())
                    .add(new Edge(destination, route));
        }
        return graph;
    }

    private List<Route> findShortestPath(Map<String, List<Edge>> graph, String source, String target) {
        Map<String, Double> distanceByNode = new HashMap<>();
        Map<String, Edge> previousEdgeByNode = new HashMap<>();
        PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparingDouble(State::distance));

        distanceByNode.put(source, 0.0);
        queue.add(new State(source, 0.0));

        while (!queue.isEmpty()) {
            State state = queue.poll();
            if (state.distance() > distanceByNode.getOrDefault(state.node(), Double.MAX_VALUE)) {
                continue;
            }
            if (target.equals(state.node())) {
                break;
            }

            for (Edge edge : graph.getOrDefault(state.node(), List.of())) {
                double nextDistance = state.distance() + routeCost(edge.route());
                if (nextDistance < distanceByNode.getOrDefault(edge.destinationNode(), Double.MAX_VALUE)) {
                    distanceByNode.put(edge.destinationNode(), nextDistance);
                    previousEdgeByNode.put(edge.destinationNode(), edge);
                    queue.add(new State(edge.destinationNode(), nextDistance));
                }
            }
        }

        if (!previousEdgeByNode.containsKey(target)) {
            return List.of();
        }

        LinkedList<Route> reversedPath = new LinkedList<>();
        String cursor = target;
        while (!source.equals(cursor)) {
            Edge edge = previousEdgeByNode.get(cursor);
            if (edge == null) {
                return List.of();
            }
            reversedPath.addFirst(edge.route());
            cursor = resolveOriginNode(edge.route());
        }
        return reversedPath;
    }

    private InternalRoutePlanResponse toResponse(
            String originPostOfficeCode,
            String destinationPostOfficeCode,
            List<Route> path
    ) {
        double totalDistance = 0.0;
        int totalDuration = 0;
        List<InternalRoutePlanResponse.Leg> legs = new ArrayList<>();
        for (int index = 0; index < path.size(); index++) {
            Route route = path.get(index);
            totalDistance += route.getEstimatedDistanceKm() == null ? 0.0 : route.getEstimatedDistanceKm();
            totalDuration += route.getEstimatedDurationMinutes() == null ? 0 : route.getEstimatedDurationMinutes();
            legs.add(new InternalRoutePlanResponse.Leg(
                    index + 1,
                    route.getId(),
                    route.getRouteCode(),
                    route.getRouteName(),
                    route.getOriginType(),
                    route.getOriginHubId(),
                    route.getOriginPostOfficeCode(),
                    route.getDestinationType(),
                    route.getDestinationHubId(),
                    route.getDestinationPostOfficeCode(),
                    route.getVehicleId(),
                    route.getEstimatedDistanceKm(),
                    route.getEstimatedDurationMinutes()
            ));
        }

        return new InternalRoutePlanResponse(
                originPostOfficeCode,
                destinationPostOfficeCode,
                true,
                totalDistance,
                totalDuration,
                legs
        );
    }

    private double routeCost(Route route) {
        if (route.getEstimatedDurationMinutes() != null && route.getEstimatedDurationMinutes() > 0) {
            return route.getEstimatedDurationMinutes();
        }
        if (route.getEstimatedDistanceKm() != null && route.getEstimatedDistanceKm() > 0) {
            return route.getEstimatedDistanceKm();
        }
        return 1.0;
    }

    private String resolveOriginNode(Route route) {
        if (route.getOriginType() == RouteEndpointType.HUB && route.getOriginHubId() != null) {
            return hubNode(route.getOriginHubId());
        }
        if (route.getOriginType() == RouteEndpointType.POST_OFFICE) {
            return postOfficeNode(route.getOriginPostOfficeCode());
        }
        return null;
    }

    private String resolveDestinationNode(Route route) {
        if (route.getDestinationType() == RouteDestinationType.HUB && route.getDestinationHubId() != null) {
            return hubNode(route.getDestinationHubId());
        }
        if (route.getDestinationType() == RouteDestinationType.POST_OFFICE) {
            return postOfficeNode(route.getDestinationPostOfficeCode());
        }
        return null;
    }

    private String hubNode(Long hubId) {
        return hubId == null ? null : HUB_PREFIX + hubId;
    }

    private String postOfficeNode(String postOfficeCode) {
        String normalized = normalizePostOfficeCode(postOfficeCode);
        return normalized == null ? null : POST_OFFICE_PREFIX + normalized;
    }

    private String normalizePostOfficeCode(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private record Edge(String destinationNode, Route route) {
    }

    private record State(String node, double distance) {
    }
}
