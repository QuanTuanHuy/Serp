/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import serp.project.first_mile.caller.DistanceMatrixCaller;
import serp.project.first_mile.caller.dto.DistanceMatrixElement;
import serp.project.first_mile.caller.dto.DistanceMatrixResult;
import serp.project.first_mile.caller.dto.GeoPoint;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.service.dto.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
class PickupOptimizationEngine {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EPSILON = 1e-9;
    private static final double HUGE_OBJECTIVE = 1e15;
    private static final double GRAMS_PER_KILOGRAM = 1000.0;

    private static final String REASON_UNASSIGNED = "UNASSIGNED";
    private static final String REASON_NO_FEASIBLE_INSERTION = "NO_FEASIBLE_INSERTION";
    private static final String REASON_MISSING_SENDER_LOCATION = "MISSING_SENDER_LOCATION";
    private static final String REASON_INVALID_SENDER_LOCATION = "INVALID_SENDER_LOCATION";

    private final DistanceMatrixCaller distanceMatrixCaller;

    PreparedOrderData prepareOrders(List<TmsOrderOperationView> candidateOrders) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();

        for (TmsOrderOperationView order : candidateOrders) {
            Point senderLocation = order.getSenderLocation();
            if (senderLocation == null) {
                unassignedOrders.add(new UnassignedOrderState(
                        toOrderNodeWithoutLocation(order),
                        REASON_MISSING_SENDER_LOCATION,
                        false
                ));
                continue;
            }

            double latitude = senderLocation.getY();
            double longitude = senderLocation.getX();
            if (!isValidCoordinate(latitude, longitude)) {
                unassignedOrders.add(new UnassignedOrderState(
                        toOrderNodeWithoutLocation(order),
                        REASON_INVALID_SENDER_LOCATION,
                        false
                ));
                continue;
            }

            assignableOrders.add(new PickupOrderNode(
                    order.getId(),
                    order.getOrderCode(),
                    order.getCustomerOrderCode(),
                    order.getSenderName(),
                    order.getSenderPhone(),
                    latitude,
                    longitude,
                    normalizeOrderWeightKg(order.getTotalWeight()),
                    safePositive(order.getTotalVolume()),
                    order.getPickupTimeStart(),
                    order.getPickupTimeEnd()
            ));
        }

        assignableOrders.sort(Comparator
                .comparing(PickupOrderNode::pickupTimeEnd, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PickupOrderNode::orderId, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return new PreparedOrderData(assignableOrders, unassignedOrders);
    }

    PickupOrderNode toOrderNodeWithoutLocation(TmsOrderOperationView order) {
        return new PickupOrderNode(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getSenderName(),
                order.getSenderPhone(),
                null,
                null,
                normalizeOrderWeightKg(order.getTotalWeight()),
                safePositive(order.getTotalVolume()),
                order.getPickupTimeStart(),
                order.getPickupTimeEnd()
        );
    }

    TravelMetricProvider buildTravelMetricProvider(
            List<PickupOrderNode> assignableOrders,
            double depotLatitude,
            double depotLongitude,
            AlgorithmConfig config
    ) {
        Map<Long, Integer> orderNodeIndexByOrderId = new HashMap<>();
        List<NodePoint> nodes = new ArrayList<>();

        nodes.add(new NodePoint(null, depotLatitude, depotLongitude));
        for (PickupOrderNode order : assignableOrders) {
            if (order.orderId() == null || orderNodeIndexByOrderId.containsKey(order.orderId())) {
                continue;
            }
            int nodeIndex = nodes.size();
            orderNodeIndexByOrderId.put(order.orderId(), nodeIndex);
            nodes.add(new NodePoint(order.orderId(), order.latitude(), order.longitude()));
        }

        int nodeCount = nodes.size();
        double[][] distanceKm = new double[nodeCount][nodeCount];
        long[][] travelMinutes = new long[nodeCount][nodeCount];

        TravelMetricProvider metricProvider = new TravelMetricProvider(
                orderNodeIndexByOrderId,
                nodes,
                distanceKm,
                travelMinutes
        );

        populateFallbackMetrics(metricProvider, config.averageSpeedKmph());
        populateDistanceMatrixMetrics(metricProvider, config);
        return metricProvider;
    }

    SolutionState buildGreedySolution(
            List<RouteState> initialRoutes,
            PreparedOrderData preparedOrderData,
            AlgorithmConfig config
    ) {
        List<RouteState> routes = new ArrayList<>();
        for (RouteState route : initialRoutes) {
            routes.add(route.copy());
        }

        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();
        for (UnassignedOrderState state : preparedOrderData.initialUnassignedOrders()) {
            unassignedOrders.add(state.copy());
        }

        for (PickupOrderNode order : preparedOrderData.assignableOrders()) {
            unassignedOrders.add(new UnassignedOrderState(order, REASON_UNASSIGNED, true));
        }

        SolutionState solution = new SolutionState(routes, unassignedOrders);
        applyGreedyRepair(solution, config);
        markNoFeasibleUnassigned(solution);
        return solution;
    }

    void applyGreedyRepair(SolutionState solution, AlgorithmConfig config) {
        while (true) {
            List<UnassignedOrderState> reinsertableOrders = getReinsertableUnassigned(solution.unassignedOrders());
            if (reinsertableOrders.isEmpty()) {
                return;
            }

            Map<Integer, Double> routeCosts = computeRouteCosts(solution.routes(), config);
            InsertionDecision bestDecision = null;

            for (UnassignedOrderState state : reinsertableOrders) {
                InsertionCandidate candidate = findBestInsertion(state.order(), solution.routes(), routeCosts, config);
                if (candidate == null) {
                    continue;
                }

                if (isBetterInsertionDecision(state.order(), candidate, bestDecision, config)) {
                    bestDecision = new InsertionDecision(state.order(), candidate);
                }
            }

            if (bestDecision == null) {
                return;
            }

            applyInsertion(solution, bestDecision.order(), bestDecision.candidate());
        }
    }

    void markNoFeasibleUnassigned(SolutionState solution) {
        for (UnassignedOrderState state : solution.unassignedOrders()) {
            if (!state.reinsertable()) {
                continue;
            }
            if (state.reason() == null || state.reason().isBlank() || REASON_UNASSIGNED.equals(state.reason())) {
                state.setReason(REASON_NO_FEASIBLE_INSERTION);
            }
        }
    }

    void sanitizeSolution(SolutionState solution) {
        Set<Long> assignedOrderIds = new HashSet<>();
        for (RouteState route : solution.routes()) {
            List<PickupOrderNode> uniqueStops = new ArrayList<>();
            for (PickupOrderNode stop : route.stops()) {
                if (stop.orderId() == null || assignedOrderIds.add(stop.orderId())) {
                    uniqueStops.add(stop);
                }
            }
            route.stops().clear();
            route.stops().addAll(uniqueStops);
        }

        List<UnassignedOrderState> filteredUnassigned = new ArrayList<>();
        Set<Long> seenUnassignedIds = new HashSet<>();
        for (UnassignedOrderState unassignedOrder : solution.unassignedOrders()) {
            Long orderId = unassignedOrder.order().orderId();
            if (orderId != null && assignedOrderIds.contains(orderId)) {
                continue;
            }
            if (orderId != null && !seenUnassignedIds.add(orderId)) {
                continue;
            }
            filteredUnassigned.add(unassignedOrder);
        }

        solution.unassignedOrders().clear();
        solution.unassignedOrders().addAll(filteredUnassigned);
    }

    SolutionEvaluation evaluateSolution(SolutionState solution, AlgorithmConfig config) {
        List<RouteEvaluation> routeEvaluations = new ArrayList<>();
        double totalDistanceKm = 0.0;
        long totalTravelMinutes = 0;
        long totalServiceMinutes = 0;
        long totalLatenessMinutes = 0;
        int assignedOrders = 0;
        int usedRoutes = 0;

        for (RouteState route : solution.routes()) {
            RouteEvaluation routeEvaluation = evaluateRoute(route, route.stops(), config);
            routeEvaluations.add(routeEvaluation);

            if (!routeEvaluation.feasible()) {
                return new SolutionEvaluation(
                        HUGE_OBJECTIVE,
                        totalDistanceKm,
                        totalTravelMinutes,
                        totalServiceMinutes,
                        totalLatenessMinutes,
                        assignedOrders,
                        solution.unassignedOrders().size(),
                        usedRoutes,
                        routeEvaluations
                );
            }

            totalDistanceKm += routeEvaluation.totalDistanceKm();
            totalTravelMinutes += routeEvaluation.totalTravelMinutes();
            totalServiceMinutes += routeEvaluation.totalServiceMinutes();
            totalLatenessMinutes += routeEvaluation.totalLatenessMinutes();
            assignedOrders += route.stops().size();
            if (!route.stops().isEmpty()) {
                usedRoutes++;
            }
        }

        int unassignedCount = solution.unassignedOrders().size();
        double objectiveScore = config.distanceWeight() * totalDistanceKm
                + config.latenessWeight() * totalLatenessMinutes
                + config.unassignedPenalty() * unassignedCount
                + config.usedRoutePenalty() * usedRoutes;

        return new SolutionEvaluation(
                objectiveScore,
                totalDistanceKm,
                totalTravelMinutes,
                totalServiceMinutes,
                totalLatenessMinutes,
                assignedOrders,
                unassignedCount,
                usedRoutes,
                routeEvaluations
        );
    }

    boolean isInfeasible(SolutionEvaluation evaluation) {
        return evaluation != null && evaluation.objectiveScore() >= HUGE_OBJECTIVE;
    }

    private void applyInsertion(SolutionState solution, PickupOrderNode order, InsertionCandidate candidate) {
        RouteState route = solution.routes().get(candidate.routeIndex());
        route.stops().add(candidate.insertPosition(), order);
        removeUnassignedOrder(solution.unassignedOrders(), order.orderId());
    }

    private boolean isBetterInsertionDecision(
            PickupOrderNode order,
            InsertionCandidate candidate,
            InsertionDecision currentBest,
            AlgorithmConfig config
    ) {
        if (currentBest == null) {
            return true;
        }

        int orderPriority = dispatchPriority(order, config);
        int bestPriority = dispatchPriority(currentBest.order(), config);
        if (orderPriority != bestPriority) {
            return orderPriority < bestPriority;
        }

        if (orderPriority == 0) {
            int pickupEndCompare = comparePickupTimeEnd(order, currentBest.order());
            if (pickupEndCompare != 0) {
                return pickupEndCompare < 0;
            }
        }

        return candidate.deltaCost() < currentBest.candidate().deltaCost();
    }

    private int dispatchPriority(PickupOrderNode order, AlgorithmConfig config) {
        return isBacklogOrder(order, config) ? 0 : 1;
    }

    private boolean isBacklogOrder(PickupOrderNode order, AlgorithmConfig config) {
        return order != null
                && order.pickupTimeEnd() != null
                && config != null
                && config.planningStartTime() != null
                && order.pickupTimeEnd().isBefore(config.planningStartTime());
    }

    private int comparePickupTimeEnd(PickupOrderNode first, PickupOrderNode second) {
        if (first.pickupTimeEnd() == null && second.pickupTimeEnd() == null) {
            return 0;
        }
        if (first.pickupTimeEnd() == null) {
            return 1;
        }
        if (second.pickupTimeEnd() == null) {
            return -1;
        }
        return first.pickupTimeEnd().compareTo(second.pickupTimeEnd());
    }

    private InsertionCandidate findBestInsertion(
            PickupOrderNode order,
            List<RouteState> routes,
            Map<Integer, Double> routeCosts,
            AlgorithmConfig config
    ) {
        InsertionCandidate bestCandidate = null;

        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            RouteState route = routes.get(routeIndex);
            double oldCost = routeCosts.getOrDefault(routeIndex, HUGE_OBJECTIVE);
            int maxInsertionPosition = route.stops().size();

            for (int insertPosition = 0; insertPosition <= maxInsertionPosition; insertPosition++) {
                List<PickupOrderNode> newStops = new ArrayList<>(route.stops());
                newStops.add(insertPosition, order);

                RouteEvaluation newEvaluation = evaluateRoute(route, newStops, config);
                if (!newEvaluation.feasible()) {
                    continue;
                }

                boolean usedRoute = !newStops.isEmpty();
                double newCost = calculateRouteCost(newEvaluation, usedRoute, config);
                double deltaCost = newCost - oldCost;

                if (bestCandidate == null || deltaCost < bestCandidate.deltaCost()) {
                    bestCandidate = new InsertionCandidate(routeIndex, insertPosition, deltaCost);
                }
            }
        }

        return bestCandidate;
    }

    private List<UnassignedOrderState> getReinsertableUnassigned(List<UnassignedOrderState> unassignedOrders) {
        List<UnassignedOrderState> result = new ArrayList<>();
        for (UnassignedOrderState state : unassignedOrders) {
            if (state.reinsertable()) {
                result.add(state);
            }
        }
        return result;
    }

    private RouteEvaluation evaluateRoute(RouteState route, List<PickupOrderNode> stops, AlgorithmConfig config) {
        if (route.vehicleId() == null) {
            return RouteEvaluation.infeasible();
        }

        if (route.maxStops() != null && route.maxStops() > 0 && stops.size() > route.maxStops()) {
            return RouteEvaluation.infeasible();
        }

        PickupOrderNode previousOrder = null;
        LocalDateTime currentTime = config.planningStartTime();

        double totalDistanceKm = 0.0;
        long totalTravelMinutes = 0;
        long totalServiceMinutes = 0;
        long totalLatenessMinutes = 0;
        double totalWeight = 0.0;
        double totalVolume = 0.0;

        List<StopEvaluationData> stopDetails = new ArrayList<>();

        for (int index = 0; index < stops.size(); index++) {
            PickupOrderNode order = stops.get(index);
            if (order.latitude() == null || order.longitude() == null) {
                return RouteEvaluation.infeasible();
            }

            LegMetric legMetric = resolveLegMetric(previousOrder, order, route, config);
            double distanceFromPreviousKm = legMetric.distanceKm();
            long travelMinutes = legMetric.travelMinutes();

            LocalDateTime arrivalTime = currentTime.plusMinutes(travelMinutes);
            LocalDateTime startServiceTime = arrivalTime;
            if (order.pickupTimeStart() != null && arrivalTime.isBefore(order.pickupTimeStart())) {
                startServiceTime = order.pickupTimeStart();
            }

            long latenessMinutes = 0;
            if (order.pickupTimeEnd() != null && startServiceTime.isAfter(order.pickupTimeEnd())) {
                latenessMinutes = ChronoUnit.MINUTES.between(order.pickupTimeEnd(), startServiceTime);
            }

            if (!config.allowLateness() && latenessMinutes > 0) {
                return RouteEvaluation.infeasible();
            }

            LocalDateTime departureTime = startServiceTime.plusMinutes(config.serviceMinutesPerStop());

            totalWeight += safePositive(order.weight());
            totalVolume += safePositive(order.volume());
            if (config.enforceCapacity()) {
                if (totalWeight > route.maxWeight() + EPSILON || totalVolume > route.maxVolume() + EPSILON) {
                    return RouteEvaluation.infeasible();
                }
            }

            totalDistanceKm += distanceFromPreviousKm;
            totalTravelMinutes += travelMinutes;
            totalServiceMinutes += config.serviceMinutesPerStop();
            totalLatenessMinutes += latenessMinutes;

            stopDetails.add(new StopEvaluationData(
                    index + 1,
                    order,
                    distanceFromPreviousKm,
                    travelMinutes,
                    arrivalTime,
                    startServiceTime,
                    departureTime,
                    latenessMinutes
            ));

            currentTime = departureTime;
            previousOrder = order;
        }

        if (!stops.isEmpty()) {
            LegMetric backLegMetric = resolveLegMetric(previousOrder, null, route, config);
            double backDistanceKm = backLegMetric.distanceKm();
            long backTravelMinutes = backLegMetric.travelMinutes();
            totalDistanceKm += backDistanceKm;
            totalTravelMinutes += backTravelMinutes;
            currentTime = currentTime.plusMinutes(backTravelMinutes);
        }

        if (config.enforcePlanningEnd() && currentTime.isAfter(config.planningEndTime())) {
            return RouteEvaluation.infeasible();
        }

        return new RouteEvaluation(
                true,
                totalDistanceKm,
                totalTravelMinutes,
                totalServiceMinutes,
                totalLatenessMinutes,
                totalWeight,
                totalVolume,
                config.planningStartTime(),
                currentTime,
                stopDetails
        );
    }

    private Map<Integer, Double> computeRouteCosts(List<RouteState> routes, AlgorithmConfig config) {
        Map<Integer, Double> routeCosts = new HashMap<>();
        for (int index = 0; index < routes.size(); index++) {
            RouteState route = routes.get(index);
            RouteEvaluation evaluation = evaluateRoute(route, route.stops(), config);
            boolean usedRoute = !route.stops().isEmpty();
            routeCosts.put(index, calculateRouteCost(evaluation, usedRoute, config));
        }
        return routeCosts;
    }

    private double calculateRouteCost(RouteEvaluation routeEvaluation, boolean usedRoute, AlgorithmConfig config) {
        if (!routeEvaluation.feasible()) {
            return HUGE_OBJECTIVE;
        }

        double cost = config.distanceWeight() * routeEvaluation.totalDistanceKm()
                + config.latenessWeight() * routeEvaluation.totalLatenessMinutes();
        if (usedRoute) {
            cost += config.usedRoutePenalty();
        }
        return cost;
    }

    private void removeUnassignedOrder(List<UnassignedOrderState> unassignedOrders, Long orderId) {
        if (orderId == null) {
            return;
        }

        for (int i = 0; i < unassignedOrders.size(); i++) {
            if (Objects.equals(unassignedOrders.get(i).order().orderId(), orderId)) {
                unassignedOrders.remove(i);
                return;
            }
        }
    }

    private void populateDistanceMatrixMetrics(TravelMetricProvider metricProvider, AlgorithmConfig config) {
        int nodeCount = metricProvider.nodeCount();
        if (nodeCount <= 1) {
            return;
        }

        if (nodeCount > config.distanceMatrixMaxNodes()) {
            log.info(
                    "Skip Goong Distance Matrix due to node count {} > max-nodes {}",
                    nodeCount,
                    config.distanceMatrixMaxNodes()
            );
            return;
        }

        int batchSize = Math.max(1, config.distanceMatrixBatchSize());
        List<GeoPoint> points = metricProvider.nodes().stream()
                .map(node -> new GeoPoint(node.latitude(), node.longitude()))
                .toList();

        for (int originStart = 0; originStart < nodeCount; originStart += batchSize) {
            int originEnd = Math.min(originStart + batchSize, nodeCount);
            List<GeoPoint> originBatch = points.subList(originStart, originEnd);

            for (int destinationStart = 0; destinationStart < nodeCount; destinationStart += batchSize) {
                int destinationEnd = Math.min(destinationStart + batchSize, nodeCount);
                List<GeoPoint> destinationBatch = points.subList(destinationStart, destinationEnd);

                DistanceMatrixResult matrixResult = distanceMatrixCaller.calculateDistanceMatrix(
                        originBatch,
                        destinationBatch,
                        config.routingVehicle()
                );

                if (!hasValidMatrixShape(matrixResult, originBatch.size(), destinationBatch.size())) {
                    log.debug(
                            "Invalid matrix shape for batch origins={} destinations={}",
                            originBatch.size(),
                            destinationBatch.size()
                    );
                    continue;
                }

                for (int originOffset = 0; originOffset < originBatch.size(); originOffset++) {
                    List<DistanceMatrixElement> row = matrixResult.rows().get(originOffset);
                    for (int destinationOffset = 0; destinationOffset < destinationBatch.size(); destinationOffset++) {
                        DistanceMatrixElement element = row.get(destinationOffset);
                        if (element == null || !element.isOk()) {
                            continue;
                        }

                        double distanceValueKm = element.distanceMeters() / 1000.0;
                        long travelMinuteValue = convertDurationToMinutes(
                                element.durationSeconds(),
                                distanceValueKm,
                                config.averageSpeedKmph()
                        );

                        metricProvider.distanceKm()[originStart + originOffset][destinationStart + destinationOffset] = distanceValueKm;
                        metricProvider.travelMinutes()[originStart + originOffset][destinationStart + destinationOffset] = travelMinuteValue;
                    }
                }
            }
        }
    }

    private boolean hasValidMatrixShape(
            DistanceMatrixResult matrixResult,
            int expectedRows,
            int expectedColumns
    ) {
        if (matrixResult == null || matrixResult.rows() == null || matrixResult.rows().size() != expectedRows) {
            return false;
        }

        for (List<DistanceMatrixElement> row : matrixResult.rows()) {
            if (row == null || row.size() != expectedColumns) {
                return false;
            }
        }

        return true;
    }

    private void populateFallbackMetrics(TravelMetricProvider metricProvider, double averageSpeedKmph) {
        int nodeCount = metricProvider.nodeCount();
        for (int fromIndex = 0; fromIndex < nodeCount; fromIndex++) {
            NodePoint fromNode = metricProvider.nodes().get(fromIndex);
            for (int toIndex = 0; toIndex < nodeCount; toIndex++) {
                NodePoint toNode = metricProvider.nodes().get(toIndex);
                if (fromIndex == toIndex) {
                    metricProvider.distanceKm()[fromIndex][toIndex] = 0.0;
                    metricProvider.travelMinutes()[fromIndex][toIndex] = 0;
                    continue;
                }

                double distanceValueKm = distanceKm(
                        fromNode.latitude(),
                        fromNode.longitude(),
                        toNode.latitude(),
                        toNode.longitude()
                );
                long travelMinuteValue = estimateTravelMinutes(distanceValueKm, averageSpeedKmph);

                metricProvider.distanceKm()[fromIndex][toIndex] = distanceValueKm;
                metricProvider.travelMinutes()[fromIndex][toIndex] = travelMinuteValue;
            }
        }
    }

    private LegMetric resolveLegMetric(
            PickupOrderNode fromOrder,
            PickupOrderNode toOrder,
            RouteState route,
            AlgorithmConfig config
    ) {
        TravelMetricProvider metricProvider = config.travelMetricProvider();
        if (metricProvider != null) {
            int fromIndex = resolveNodeIndex(fromOrder, metricProvider);
            int toIndex = resolveNodeIndex(toOrder, metricProvider);
            if (fromIndex >= 0 && toIndex >= 0
                    && fromIndex < metricProvider.nodeCount()
                    && toIndex < metricProvider.nodeCount()) {
                return new LegMetric(
                        metricProvider.distanceKm()[fromIndex][toIndex],
                        metricProvider.travelMinutes()[fromIndex][toIndex]
                );
            }
        }

        double fromLatitude = fromOrder == null ? route.depotLatitude() : fromOrder.latitude();
        double fromLongitude = fromOrder == null ? route.depotLongitude() : fromOrder.longitude();
        double toLatitude = toOrder == null ? route.depotLatitude() : toOrder.latitude();
        double toLongitude = toOrder == null ? route.depotLongitude() : toOrder.longitude();

        double distanceValueKm = distanceKm(fromLatitude, fromLongitude, toLatitude, toLongitude);
        long travelMinuteValue = estimateTravelMinutes(distanceValueKm, config.averageSpeedKmph());
        return new LegMetric(distanceValueKm, travelMinuteValue);
    }

    private int resolveNodeIndex(PickupOrderNode order, TravelMetricProvider metricProvider) {
        if (order == null) {
            return 0;
        }
        if (order.orderId() == null) {
            return -1;
        }
        return metricProvider.orderNodeIndexByOrderId().getOrDefault(order.orderId(), -1);
    }

    private long convertDurationToMinutes(Long durationSeconds, double distanceKm, double averageSpeedKmph) {
        if (durationSeconds == null || durationSeconds < 0) {
            return estimateTravelMinutes(distanceKm, averageSpeedKmph);
        }

        if (durationSeconds == 0) {
            return distanceKm <= EPSILON ? 0 : 1;
        }

        return Math.max(1L, Math.round(durationSeconds / 60.0));
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    private long estimateTravelMinutes(double distanceKm, double averageSpeedKmph) {
        if (distanceKm <= EPSILON) {
            return 0;
        }

        double hours = distanceKm / averageSpeedKmph;
        long minutes = Math.round(hours * 60.0);
        return Math.max(minutes, 1L);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double safePositive(Double value) {
        if (value == null || value < 0.0) {
            return 0.0;
        }
        return value;
    }

    private double normalizeOrderWeightKg(Double weightGram) {
        return safePositive(weightGram) / GRAMS_PER_KILOGRAM;
    }
}
