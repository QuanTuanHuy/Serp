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
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.enums.PickupDestroyOperator;
import serp.project.first_mile.enums.PickupRepairOperator;
import serp.project.first_mile.service.dto.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
class PickupOptimizationEngine {

    private static final double OPERATOR_REACTION_FACTOR = 0.2;
    private static final double OPERATOR_MIN_WEIGHT = 0.1;
    private static final int OPERATOR_UPDATE_SEGMENT = 25;

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EPSILON = 1e-9;
    private static final double HUGE_OBJECTIVE = 1e15;

    private static final String REASON_UNASSIGNED = "UNASSIGNED";
    private static final String REASON_NO_FEASIBLE_INSERTION = "NO_FEASIBLE_INSERTION";
    private static final String REASON_ALNS_REMOVAL = "REMOVED_BY_ALNS";
    private static final String REASON_MISSING_SENDER_LOCATION = "MISSING_SENDER_LOCATION";
    private static final String REASON_INVALID_SENDER_LOCATION = "INVALID_SENDER_LOCATION";

    private final DistanceMatrixCaller distanceMatrixCaller;

    PreparedOrderData prepareOrders(List<Order> candidateOrders) {
        List<PickupOrderNode> assignableOrders = new ArrayList<>();
        List<UnassignedOrderState> unassignedOrders = new ArrayList<>();

        for (Order order : candidateOrders) {
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
                    safePositive(order.getTotalWeight()),
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

    PickupOrderNode toOrderNodeWithoutLocation(Order order) {
        return new PickupOrderNode(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getSenderName(),
                order.getSenderPhone(),
                null,
                null,
                safePositive(order.getTotalWeight()),
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

    SolutionState buildInitialSolution(
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

    SolutionState runAlns(SolutionState initialSolution, AlgorithmConfig config) {
        Random random = new Random();
        SolutionState currentSolution = deepCopySolution(initialSolution);
        SolutionState bestSolution = deepCopySolution(initialSolution);

        SolutionEvaluation currentEvaluation = evaluateSolution(currentSolution, config);
        SolutionEvaluation bestEvaluation = currentEvaluation;

        EnumMap<PickupDestroyOperator, Double> destroyWeights = initializeDestroyWeights();
        EnumMap<PickupDestroyOperator, Double> destroyScores = initializeDestroyWeights();
        EnumMap<PickupDestroyOperator, Integer> destroyUsages = initializeDestroyUsages();

        EnumMap<PickupRepairOperator, Double> repairWeights = initializeRepairWeights();
        EnumMap<PickupRepairOperator, Double> repairScores = initializeRepairWeights();
        EnumMap<PickupRepairOperator, Integer> repairUsages = initializeRepairUsages();

        long deadlineNanos = System.nanoTime() + config.maxRuntimeMillis() * 1_000_000L;
        double temperature = config.initialTemperature();

        for (int iteration = 1; iteration <= config.maxIterations(); iteration++) {
            if (System.nanoTime() > deadlineNanos) {
                break;
            }

            PickupDestroyOperator destroyOperator = selectDestroyOperator(destroyWeights, random);
            PickupRepairOperator repairOperator = selectRepairOperator(repairWeights, random);

            destroyUsages.put(destroyOperator, destroyUsages.get(destroyOperator) + 1);
            repairUsages.put(repairOperator, repairUsages.get(repairOperator) + 1);

            SolutionState candidateSolution = deepCopySolution(currentSolution);
            applyDestroy(candidateSolution, destroyOperator, random, config);
            applyRepair(candidateSolution, repairOperator, config);
            markNoFeasibleUnassigned(candidateSolution);

            SolutionEvaluation candidateEvaluation = evaluateSolution(candidateSolution, config);
            double previousCurrentObjective = currentEvaluation.objectiveScore();
            boolean accepted = shouldAccept(
                    candidateEvaluation.objectiveScore(),
                    previousCurrentObjective,
                    temperature,
                    random
            );

            if (accepted) {
                currentSolution = candidateSolution;
                currentEvaluation = candidateEvaluation;
            }

            double reward = 0.0;
            if (candidateEvaluation.objectiveScore() + EPSILON < bestEvaluation.objectiveScore()) {
                bestSolution = candidateSolution;
                bestEvaluation = candidateEvaluation;
                reward = 8.0;
            } else if (candidateEvaluation.objectiveScore() + EPSILON < previousCurrentObjective) {
                reward = 4.0;
            } else if (accepted) {
                reward = 1.0;
            }

            destroyScores.put(destroyOperator, destroyScores.get(destroyOperator) + reward);
            repairScores.put(repairOperator, repairScores.get(repairOperator) + reward);

            if (iteration % OPERATOR_UPDATE_SEGMENT == 0) {
                updateDestroyWeights(destroyWeights, destroyScores, destroyUsages);
                updateRepairWeights(repairWeights, repairScores, repairUsages);
            }

            temperature = Math.max(0.0001, temperature * config.coolingRate());
        }

        return bestSolution;
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

                if (bestDecision == null || candidate.deltaCost() < bestDecision.candidate().deltaCost()) {
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

    private void applyDestroy(
            SolutionState solution,
            PickupDestroyOperator destroyOperator,
            Random random,
            AlgorithmConfig config
    ) {
        int totalAssignedOrders = countAssignedOrders(solution.routes());
        if (totalAssignedOrders <= 0) {
            return;
        }

        int removeCount = Math.max(1, (int) Math.round(totalAssignedOrders * config.destroyRate()));
        removeCount = Math.min(removeCount, totalAssignedOrders);

        if (destroyOperator == PickupDestroyOperator.WORST) {
            applyWorstDestroy(solution, removeCount, random, config);
            return;
        }

        applyRandomDestroy(solution, removeCount, random);
    }

    private void applyRandomDestroy(SolutionState solution, int removeCount, Random random) {
        List<RouteOrderRef> orderRefs = collectAssignedOrderRefs(solution.routes());
        if (orderRefs.isEmpty()) {
            return;
        }

        Collections.shuffle(orderRefs, random);
        List<RouteOrderRef> selected = new ArrayList<>(orderRefs.subList(0, Math.min(removeCount, orderRefs.size())));
        selected.sort(Comparator
                .comparingInt(RouteOrderRef::routeIndex)
                .thenComparing(RouteOrderRef::stopIndex)
                .reversed());

        for (RouteOrderRef ref : selected) {
            removeAssignedOrder(solution, ref.routeIndex(), ref.stopIndex(), REASON_ALNS_REMOVAL);
        }
    }

    private void applyWorstDestroy(SolutionState solution, int removeCount, Random random, AlgorithmConfig config) {
        List<RouteOrderContribution> contributions = new ArrayList<>();

        for (int routeIndex = 0; routeIndex < solution.routes().size(); routeIndex++) {
            RouteState route = solution.routes().get(routeIndex);
            for (int stopIndex = 0; stopIndex < route.stops().size(); stopIndex++) {
                double contribution = estimateMarginalDistanceContribution(route, stopIndex, config);
                contribution += random.nextDouble() * 0.0001;
                contributions.add(new RouteOrderContribution(routeIndex, stopIndex, contribution));
            }
        }

        if (contributions.isEmpty()) {
            return;
        }

        contributions.sort(Comparator.comparingDouble(RouteOrderContribution::contribution).reversed());

        List<RouteOrderRef> selected = new ArrayList<>();
        int upperBound = Math.min(removeCount, contributions.size());
        for (int i = 0; i < upperBound; i++) {
            RouteOrderContribution contribution = contributions.get(i);
            selected.add(new RouteOrderRef(contribution.routeIndex(), contribution.stopIndex()));
        }

        selected.sort(Comparator
                .comparingInt(RouteOrderRef::routeIndex)
                .thenComparing(RouteOrderRef::stopIndex)
                .reversed());

        for (RouteOrderRef ref : selected) {
            removeAssignedOrder(solution, ref.routeIndex(), ref.stopIndex(), REASON_ALNS_REMOVAL);
        }
    }

    private void removeAssignedOrder(SolutionState solution, int routeIndex, int stopIndex, String reason) {
        if (routeIndex < 0 || routeIndex >= solution.routes().size()) {
            return;
        }

        RouteState route = solution.routes().get(routeIndex);
        if (stopIndex < 0 || stopIndex >= route.stops().size()) {
            return;
        }

        PickupOrderNode removedOrder = route.stops().remove(stopIndex);
        removeUnassignedOrder(solution.unassignedOrders(), removedOrder.orderId());
        solution.unassignedOrders().add(new UnassignedOrderState(removedOrder, reason, true));
    }

    private void applyRepair(
            SolutionState solution,
            PickupRepairOperator repairOperator,
            AlgorithmConfig config
    ) {
        if (repairOperator == PickupRepairOperator.REGRET_2) {
            applyRegret2Repair(solution, config);
            return;
        }

        applyGreedyRepair(solution, config);
    }

    private void applyRegret2Repair(SolutionState solution, AlgorithmConfig config) {
        while (true) {
            List<UnassignedOrderState> reinsertableOrders = getReinsertableUnassigned(solution.unassignedOrders());
            if (reinsertableOrders.isEmpty()) {
                return;
            }

            Map<Integer, Double> routeCosts = computeRouteCosts(solution.routes(), config);
            RegretDecision bestDecision = null;

            for (UnassignedOrderState state : reinsertableOrders) {
                List<InsertionCandidate> topCandidates = findTopInsertionCandidates(
                        state.order(),
                        solution.routes(),
                        routeCosts,
                        config,
                        2
                );
                if (topCandidates.isEmpty()) {
                    continue;
                }

                InsertionCandidate bestCandidate = topCandidates.get(0);
                double secondCost = topCandidates.size() > 1 ? topCandidates.get(1).deltaCost() : bestCandidate.deltaCost() + 100.0;
                double regretValue = secondCost - bestCandidate.deltaCost();

                RegretDecision currentDecision = new RegretDecision(state.order(), bestCandidate, regretValue);
                if (bestDecision == null) {
                    bestDecision = currentDecision;
                    continue;
                }

                if (currentDecision.regretValue() > bestDecision.regretValue() + EPSILON) {
                    bestDecision = currentDecision;
                    continue;
                }

                if (Math.abs(currentDecision.regretValue() - bestDecision.regretValue()) <= EPSILON
                        && currentDecision.candidate().deltaCost() < bestDecision.candidate().deltaCost()) {
                    bestDecision = currentDecision;
                }
            }

            if (bestDecision == null) {
                return;
            }

            applyInsertion(solution, bestDecision.order(), bestDecision.candidate());
        }
    }

    private void applyInsertion(SolutionState solution, PickupOrderNode order, InsertionCandidate candidate) {
        RouteState route = solution.routes().get(candidate.routeIndex());
        route.stops().add(candidate.insertPosition(), order);
        removeUnassignedOrder(solution.unassignedOrders(), order.orderId());
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

    private List<InsertionCandidate> findTopInsertionCandidates(
            PickupOrderNode order,
            List<RouteState> routes,
            Map<Integer, Double> routeCosts,
            AlgorithmConfig config,
            int topN
    ) {
        List<InsertionCandidate> candidates = new ArrayList<>();

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
                candidates.add(new InsertionCandidate(routeIndex, insertPosition, deltaCost));
            }
        }

        candidates.sort(Comparator.comparingDouble(InsertionCandidate::deltaCost));
        if (candidates.size() <= topN) {
            return candidates;
        }

        return new ArrayList<>(candidates.subList(0, topN));
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

    private SolutionState deepCopySolution(SolutionState source) {
        List<RouteState> copiedRoutes = new ArrayList<>();
        for (RouteState route : source.routes()) {
            copiedRoutes.add(route.copy());
        }

        List<UnassignedOrderState> copiedUnassigned = new ArrayList<>();
        for (UnassignedOrderState unassignedOrder : source.unassignedOrders()) {
            copiedUnassigned.add(unassignedOrder.copy());
        }

        return new SolutionState(copiedRoutes, copiedUnassigned);
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

    private boolean shouldAccept(double candidateObjective, double currentObjective, double temperature, Random random) {
        if (candidateObjective + EPSILON < currentObjective) {
            return true;
        }

        if (temperature <= EPSILON) {
            return false;
        }

        double probability = Math.exp((currentObjective - candidateObjective) / temperature);
        return random.nextDouble() < probability;
    }

    private List<RouteOrderRef> collectAssignedOrderRefs(List<RouteState> routes) {
        List<RouteOrderRef> refs = new ArrayList<>();
        for (int routeIndex = 0; routeIndex < routes.size(); routeIndex++) {
            RouteState route = routes.get(routeIndex);
            for (int stopIndex = 0; stopIndex < route.stops().size(); stopIndex++) {
                refs.add(new RouteOrderRef(routeIndex, stopIndex));
            }
        }
        return refs;
    }

    private int countAssignedOrders(List<RouteState> routes) {
        int total = 0;
        for (RouteState route : routes) {
            total += route.stops().size();
        }
        return total;
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

    private double estimateMarginalDistanceContribution(RouteState route, int stopIndex, AlgorithmConfig config) {
        PickupOrderNode current = route.stops().get(stopIndex);

        PickupOrderNode previous;
        if (stopIndex == 0) {
            previous = null;
        } else {
            previous = route.stops().get(stopIndex - 1);
        }

        PickupOrderNode next;
        if (stopIndex == route.stops().size() - 1) {
            next = null;
        } else {
            next = route.stops().get(stopIndex + 1);
        }

        double viaCurrent = resolveLegMetric(previous, current, route, config).distanceKm()
                + resolveLegMetric(current, next, route, config).distanceKm();
        double direct = resolveLegMetric(previous, next, route, config).distanceKm();
        return viaCurrent - direct;
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
        List<DistanceMatrixCaller.GeoPoint> points = metricProvider.nodes().stream()
                .map(node -> new DistanceMatrixCaller.GeoPoint(node.latitude(), node.longitude()))
                .toList();

        for (int originStart = 0; originStart < nodeCount; originStart += batchSize) {
            int originEnd = Math.min(originStart + batchSize, nodeCount);
            List<DistanceMatrixCaller.GeoPoint> originBatch = points.subList(originStart, originEnd);

            for (int destinationStart = 0; destinationStart < nodeCount; destinationStart += batchSize) {
                int destinationEnd = Math.min(destinationStart + batchSize, nodeCount);
                List<DistanceMatrixCaller.GeoPoint> destinationBatch = points.subList(destinationStart, destinationEnd);

                DistanceMatrixCaller.DistanceMatrixResult matrixResult = distanceMatrixCaller.calculateDistanceMatrix(
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
                    List<DistanceMatrixCaller.DistanceMatrixElement> row = matrixResult.rows().get(originOffset);
                    for (int destinationOffset = 0; destinationOffset < destinationBatch.size(); destinationOffset++) {
                        DistanceMatrixCaller.DistanceMatrixElement element = row.get(destinationOffset);
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
            DistanceMatrixCaller.DistanceMatrixResult matrixResult,
            int expectedRows,
            int expectedColumns
    ) {
        if (matrixResult == null || matrixResult.rows() == null || matrixResult.rows().size() != expectedRows) {
            return false;
        }

        for (List<DistanceMatrixCaller.DistanceMatrixElement> row : matrixResult.rows()) {
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

    private EnumMap<PickupDestroyOperator, Double> initializeDestroyWeights() {
        EnumMap<PickupDestroyOperator, Double> weights = new EnumMap<>(PickupDestroyOperator.class);
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            weights.put(operator, 1.0);
        }
        return weights;
    }

    private EnumMap<PickupDestroyOperator, Integer> initializeDestroyUsages() {
        EnumMap<PickupDestroyOperator, Integer> usages = new EnumMap<>(PickupDestroyOperator.class);
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            usages.put(operator, 0);
        }
        return usages;
    }

    private EnumMap<PickupRepairOperator, Double> initializeRepairWeights() {
        EnumMap<PickupRepairOperator, Double> weights = new EnumMap<>(PickupRepairOperator.class);
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            weights.put(operator, 1.0);
        }
        return weights;
    }

    private EnumMap<PickupRepairOperator, Integer> initializeRepairUsages() {
        EnumMap<PickupRepairOperator, Integer> usages = new EnumMap<>(PickupRepairOperator.class);
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            usages.put(operator, 0);
        }
        return usages;
    }

    private PickupDestroyOperator selectDestroyOperator(EnumMap<PickupDestroyOperator, Double> weights, Random random) {
        return rouletteSelect(weights, random, PickupDestroyOperator.RANDOM);
    }

    private PickupRepairOperator selectRepairOperator(EnumMap<PickupRepairOperator, Double> weights, Random random) {
        return rouletteSelect(weights, random, PickupRepairOperator.GREEDY);
    }

    private <T extends Enum<T>> T rouletteSelect(EnumMap<T, Double> weights, Random random, T fallback) {
        double totalWeight = 0.0;
        for (double weight : weights.values()) {
            totalWeight += Math.max(weight, 0.0);
        }

        if (totalWeight <= EPSILON) {
            return fallback;
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (Map.Entry<T, Double> entry : weights.entrySet()) {
            cumulative += Math.max(entry.getValue(), 0.0);
            if (randomValue <= cumulative) {
                return entry.getKey();
            }
        }

        return fallback;
    }

    private void updateDestroyWeights(
            EnumMap<PickupDestroyOperator, Double> weights,
            EnumMap<PickupDestroyOperator, Double> scores,
            EnumMap<PickupDestroyOperator, Integer> usages
    ) {
        for (PickupDestroyOperator operator : PickupDestroyOperator.values()) {
            double averageScore = usages.get(operator) == 0
                    ? 0.0
                    : scores.get(operator) / usages.get(operator);
            double updatedWeight = (1 - OPERATOR_REACTION_FACTOR) * weights.get(operator)
                    + OPERATOR_REACTION_FACTOR * Math.max(OPERATOR_MIN_WEIGHT, averageScore);
            weights.put(operator, updatedWeight);
            scores.put(operator, 0.0);
            usages.put(operator, 0);
        }
    }

    private void updateRepairWeights(
            EnumMap<PickupRepairOperator, Double> weights,
            EnumMap<PickupRepairOperator, Double> scores,
            EnumMap<PickupRepairOperator, Integer> usages
    ) {
        for (PickupRepairOperator operator : PickupRepairOperator.values()) {
            double averageScore = usages.get(operator) == 0
                    ? 0.0
                    : scores.get(operator) / usages.get(operator);
            double updatedWeight = (1 - OPERATOR_REACTION_FACTOR) * weights.get(operator)
                    + OPERATOR_REACTION_FACTOR * Math.max(OPERATOR_MIN_WEIGHT, averageScore);
            weights.put(operator, updatedWeight);
            scores.put(operator, 0.0);
            usages.put(operator, 0);
        }
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
}
