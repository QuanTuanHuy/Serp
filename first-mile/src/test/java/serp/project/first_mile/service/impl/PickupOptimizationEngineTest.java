/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.first_mile.caller.DistanceMatrixCaller;
import serp.project.first_mile.caller.dto.DistanceMatrixElement;
import serp.project.first_mile.caller.dto.DistanceMatrixResult;
import serp.project.first_mile.caller.dto.GeoPoint;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.enums.RoutingVehicle;
import serp.project.first_mile.service.dto.AlgorithmConfig;
import serp.project.first_mile.service.dto.NodePoint;
import serp.project.first_mile.service.dto.PickupOrderNode;
import serp.project.first_mile.service.dto.PreparedOrderData;
import serp.project.first_mile.service.dto.RouteEvaluation;
import serp.project.first_mile.service.dto.RouteState;
import serp.project.first_mile.service.dto.SolutionEvaluation;
import serp.project.first_mile.service.dto.SolutionState;
import serp.project.first_mile.service.dto.StopEvaluationData;
import serp.project.first_mile.service.dto.TravelMetricProvider;
import serp.project.first_mile.service.dto.UnassignedOrderState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickupOptimizationEngineTest {

    private static final LocalDateTime PLANNING_START = LocalDateTime.of(2026, 6, 10, 7, 30);
    private static final LocalDateTime PLANNING_END = LocalDateTime.of(2026, 6, 10, 12, 0);

    private final PickupOptimizationEngine engine = new PickupOptimizationEngine(null);

    @Test
    void prepareOrdersConvertsOrderWeightGramToKilogramAndKeepsVolumeM3() {
        PreparedOrderData preparedOrderData = engine.prepareOrders(List.of(
                order(1L, 5000.0, 0.25)
        ));

        PickupOrderNode order = preparedOrderData.assignableOrders().getFirst();

        assertEquals(5.0, order.weight(), 0.0001);
        assertEquals(0.25, order.volume(), 0.0001);
    }

    @Test
    void prepareOrdersTreatsNullAndNegativeWeightOrVolumeAsZero() {
        PreparedOrderData preparedOrderData = engine.prepareOrders(List.of(
                order(1L, null, null),
                order(2L, -5000.0, -0.25)
        ));

        assertEquals(0.0, preparedOrderData.assignableOrders().get(0).weight(), 0.0001);
        assertEquals(0.0, preparedOrderData.assignableOrders().get(0).volume(), 0.0001);
        assertEquals(0.0, preparedOrderData.assignableOrders().get(1).weight(), 0.0001);
        assertEquals(0.0, preparedOrderData.assignableOrders().get(1).volume(), 0.0001);
    }

    @Test
    void prepareOrdersSeparatesMissingAndInvalidSenderLocationsAndSortsAssignableOrders() {
        LocalDateTime earlyEnd = PLANNING_START.plusHours(1);
        LocalDateTime lateEnd = PLANNING_START.plusHours(2);

        PreparedOrderData preparedOrderData = engine.prepareOrders(List.of(
                order(4L, 1000.0, 0.1, 10.004, 106.004, PLANNING_START, earlyEnd),
                order(1L, 1000.0, 0.1, null, 106.001, PLANNING_START, earlyEnd),
                order(2L, 1000.0, 0.1, 91.0, 106.002, PLANNING_START, earlyEnd),
                order(3L, 1000.0, 0.1, 10.003, 106.003, PLANNING_START, earlyEnd),
                order(5L, 1000.0, 0.1, 10.005, 106.005, PLANNING_START, lateEnd)
        ));

        assertEquals(List.of(3L, 4L, 5L), orderIds(preparedOrderData.assignableOrders()));
        assertEquals(2, preparedOrderData.initialUnassignedOrders().size());
        assertEquals(1L, preparedOrderData.initialUnassignedOrders().get(0).order().orderId());
        assertEquals("MISSING_SENDER_LOCATION", preparedOrderData.initialUnassignedOrders().get(0).reason());
        assertFalse(preparedOrderData.initialUnassignedOrders().get(0).reinsertable());
        assertNull(preparedOrderData.initialUnassignedOrders().get(0).order().latitude());
        assertEquals(2L, preparedOrderData.initialUnassignedOrders().get(1).order().orderId());
        assertEquals("INVALID_SENDER_LOCATION", preparedOrderData.initialUnassignedOrders().get(1).reason());
        assertFalse(preparedOrderData.initialUnassignedOrders().get(1).reinsertable());
    }

    @Test
    void buildTravelMetricProviderIndexesUniqueOrdersAndSkipsNullOrderIds() {
        RecordingDistanceMatrixCaller caller = new RecordingDistanceMatrixCaller(null);
        PickupOptimizationEngine metricEngine = new PickupOptimizationEngine(caller);

        TravelMetricProvider metricProvider = metricEngine.buildTravelMetricProvider(
                List.of(
                        orderNode(1L),
                        orderNode(1L),
                        orderNode(null)
                ),
                10.0,
                106.0,
                configWithMatrixMaxNodes(1)
        );

        assertEquals(0, caller.calls);
        assertEquals(2, metricProvider.nodeCount());
        assertEquals(Map.of(1L, 1), metricProvider.orderNodeIndexByOrderId());
        assertTrue(metricProvider.distanceKm()[0][1] > 0.0);
        assertTrue(metricProvider.travelMinutes()[0][1] > 0);
    }

    @Test
    void buildTravelMetricProviderOverwritesFallbackMetricsWithValidDistanceMatrixValues() {
        RecordingDistanceMatrixCaller caller = new RecordingDistanceMatrixCaller(new DistanceMatrixResult(List.of(
                List.of(
                        new DistanceMatrixElement("OK", 0L, 0L),
                        new DistanceMatrixElement("OK", 180L, 1000L)
                ),
                List.of(
                        new DistanceMatrixElement("OK", 240L, 2000L),
                        new DistanceMatrixElement("OK", 0L, 0L)
                )
        )));
        PickupOptimizationEngine metricEngine = new PickupOptimizationEngine(caller);

        TravelMetricProvider metricProvider = metricEngine.buildTravelMetricProvider(
                List.of(orderNode(1L)),
                10.0,
                106.0,
                config()
        );

        assertEquals(1, caller.calls);
        assertEquals(RoutingVehicle.BIKE, caller.lastVehicle);
        assertEquals(2, caller.lastOrigins.size());
        assertEquals(2, caller.lastDestinations.size());
        assertEquals(1.0, metricProvider.distanceKm()[0][1], 0.0001);
        assertEquals(3, metricProvider.travelMinutes()[0][1]);
        assertEquals(2.0, metricProvider.distanceKm()[1][0], 0.0001);
        assertEquals(4, metricProvider.travelMinutes()[1][0]);
    }

    @Test
    void buildTravelMetricProviderKeepsFallbackMetricsWhenDistanceMatrixResponseIsInvalid() {
        PickupOrderNode order = orderNode(1L);
        TravelMetricProvider fallbackProvider = engine.buildTravelMetricProvider(
                List.of(order),
                10.0,
                106.0,
                configWithMatrixMaxNodes(1)
        );
        RecordingDistanceMatrixCaller caller = new RecordingDistanceMatrixCaller(new DistanceMatrixResult(List.of()));
        PickupOptimizationEngine metricEngine = new PickupOptimizationEngine(caller);

        TravelMetricProvider metricProvider = metricEngine.buildTravelMetricProvider(
                List.of(order),
                10.0,
                106.0,
                config()
        );

        assertEquals(1, caller.calls);
        assertEquals(fallbackProvider.distanceKm()[0][1], metricProvider.distanceKm()[0][1], 0.0001);
        assertEquals(fallbackProvider.travelMinutes()[0][1], metricProvider.travelMinutes()[0][1]);
    }

    @Test
    void buildGreedySolutionAssignsFeasibleOrdersToCheapestRouteAndClearsUnassignedEntries() {
        PickupOrderNode firstOrder = orderNode(1L);
        PickupOrderNode secondOrder = orderNode(2L);
        TravelMetricProvider metricProvider = metricProvider(
                List.of(firstOrder, secondOrder),
                new double[][]{
                        {0.0, 1.0, 3.0},
                        {1.0, 0.0, 1.0},
                        {1.0, 1.0, 0.0}
                },
                new long[][]{
                        {0, 1, 3},
                        {1, 0, 1},
                        {1, 1, 0}
                }
        );

        SolutionState solution = engine.buildGreedySolution(
                List.of(routeWithCapacity(10.0, 1.0, List.of())),
                new PreparedOrderData(List.of(firstOrder, secondOrder), List.of()),
                config(metricProvider)
        );

        assertEquals(List.of(1L, 2L), orderIds(solution.routes().getFirst().stops()));
        assertTrue(solution.unassignedOrders().isEmpty());
    }

    @Test
    void buildGreedySolutionSplitsOrdersAcrossRoutesWhenCapacityWouldBeExceeded() {
        PickupOrderNode firstOrder = orderNode(1L, 5.0, 0.1);
        PickupOrderNode secondOrder = orderNode(2L, 5.0, 0.1);
        TravelMetricProvider metricProvider = simpleMetricProvider(List.of(firstOrder, secondOrder));

        SolutionState solution = engine.buildGreedySolution(
                List.of(
                        route(10L, 100L, 20, 5.0, 1.0, List.of()),
                        route(11L, 101L, 20, 5.0, 1.0, List.of())
                ),
                new PreparedOrderData(List.of(firstOrder, secondOrder), List.of()),
                config(metricProvider)
        );

        assertEquals(1, solution.routes().get(0).stops().size());
        assertEquals(1, solution.routes().get(1).stops().size());
        assertTrue(solution.unassignedOrders().isEmpty());
    }

    @Test
    void buildGreedySolutionMarksNoFeasibleInsertionWhenRouteStopLimitIsReached() {
        PickupOrderNode firstOrder = orderNode(1L);
        PickupOrderNode secondOrder = orderNode(2L);
        TravelMetricProvider metricProvider = simpleMetricProvider(List.of(firstOrder, secondOrder));

        SolutionState solution = engine.buildGreedySolution(
                List.of(route(10L, 100L, 1, 10.0, 1.0, List.of())),
                new PreparedOrderData(List.of(firstOrder, secondOrder), List.of()),
                config(metricProvider)
        );

        assertEquals(1, solution.routes().getFirst().stops().size());
        assertEquals(1, solution.unassignedOrders().size());
        assertEquals("NO_FEASIBLE_INSERTION", solution.unassignedOrders().getFirst().reason());
        assertTrue(solution.unassignedOrders().getFirst().reinsertable());
    }

    @Test
    void buildGreedySolutionPrioritizesBacklogOrderWhenRouteStopLimitIsReached() {
        PickupOrderNode regularOrder = orderNode(1L);
        PickupOrderNode backlogOrder = orderNodeWithWindow(
                2L,
                PLANNING_START.minusHours(2),
                PLANNING_START.minusMinutes(30)
        );
        TravelMetricProvider metricProvider = simpleMetricProvider(List.of(regularOrder, backlogOrder));

        SolutionState solution = engine.buildGreedySolution(
                List.of(route(10L, 100L, 1, 10.0, 1.0, List.of())),
                new PreparedOrderData(List.of(regularOrder, backlogOrder), List.of()),
                config(metricProvider)
        );

        assertEquals(List.of(2L), orderIds(solution.routes().getFirst().stops()));
        assertEquals(1, solution.unassignedOrders().size());
        assertEquals(1L, solution.unassignedOrders().getFirst().order().orderId());
    }

    @Test
    void buildGreedySolutionKeepsInitialUnassignedOrdersWithoutReinsertingThem() {
        UnassignedOrderState initialUnassigned = new UnassignedOrderState(
                orderNodeWithoutLocation(99L),
                "MISSING_SENDER_LOCATION",
                false
        );

        SolutionState solution = engine.buildGreedySolution(
                List.of(routeWithCapacity(10.0, 1.0, List.of())),
                new PreparedOrderData(List.of(), List.of(initialUnassigned)),
                config()
        );

        assertTrue(solution.routes().getFirst().stops().isEmpty());
        assertEquals(1, solution.unassignedOrders().size());
        assertEquals(99L, solution.unassignedOrders().getFirst().order().orderId());
        assertEquals("MISSING_SENDER_LOCATION", solution.unassignedOrders().getFirst().reason());
        assertFalse(solution.unassignedOrders().getFirst().reinsertable());
    }

    @Test
    void applyGreedyRepairChoosesBestInsertionPositionForExistingRoute() {
        PickupOrderNode existingOrder = orderNode(1L);
        PickupOrderNode insertedOrder = orderNode(2L);
        TravelMetricProvider metricProvider = metricProvider(
                List.of(existingOrder, insertedOrder),
                new double[][]{
                        {0.0, 10.0, 1.0},
                        {10.0, 0.0, 20.0},
                        {1.0, 1.0, 0.0}
                },
                new long[][]{
                        {0, 10, 1},
                        {10, 0, 20},
                        {1, 1, 0}
                }
        );
        SolutionState solution = new SolutionState(
                List.of(routeWithCapacity(10.0, 1.0, List.of(existingOrder))),
                new ArrayList<>(List.of(new UnassignedOrderState(insertedOrder, "UNASSIGNED", true)))
        );

        engine.applyGreedyRepair(solution, config(metricProvider));

        assertEquals(List.of(2L, 1L), orderIds(solution.routes().getFirst().stops()));
        assertTrue(solution.unassignedOrders().isEmpty());
    }

    @Test
    void markNoFeasibleUnassignedOnlyUpdatesBlankOrDefaultReinsertableReasons() {
        UnassignedOrderState nullReason = new UnassignedOrderState(orderNode(1L), null, true);
        UnassignedOrderState blankReason = new UnassignedOrderState(orderNode(2L), " ", true);
        UnassignedOrderState defaultReason = new UnassignedOrderState(orderNode(3L), "UNASSIGNED", true);
        UnassignedOrderState specificReason = new UnassignedOrderState(orderNode(4L), "CUSTOM_REASON", true);
        UnassignedOrderState nonReinsertable = new UnassignedOrderState(orderNode(5L), "UNASSIGNED", false);
        SolutionState solution = new SolutionState(
                List.of(),
                new ArrayList<>(List.of(nullReason, blankReason, defaultReason, specificReason, nonReinsertable))
        );

        engine.markNoFeasibleUnassigned(solution);

        assertEquals("NO_FEASIBLE_INSERTION", nullReason.reason());
        assertEquals("NO_FEASIBLE_INSERTION", blankReason.reason());
        assertEquals("NO_FEASIBLE_INSERTION", defaultReason.reason());
        assertEquals("CUSTOM_REASON", specificReason.reason());
        assertEquals("UNASSIGNED", nonReinsertable.reason());
    }

    @Test
    void sanitizeSolutionRemovesDuplicateAssignedStopsAndAssignedOrDuplicateUnassignedOrders() {
        PickupOrderNode firstOrder = orderNode(1L);
        PickupOrderNode secondOrder = orderNode(2L);
        PickupOrderNode thirdOrder = orderNode(3L);
        PickupOrderNode fourthOrder = orderNode(4L);
        SolutionState solution = new SolutionState(
                List.of(
                        routeWithCapacity(10.0, 1.0, List.of(firstOrder, secondOrder)),
                        route(11L, 101L, 20, 10.0, 1.0, List.of(secondOrder, thirdOrder))
                ),
                new ArrayList<>(List.of(
                        new UnassignedOrderState(firstOrder, "UNASSIGNED", true),
                        new UnassignedOrderState(fourthOrder, "UNASSIGNED", true),
                        new UnassignedOrderState(fourthOrder, "UNASSIGNED", true)
                ))
        );

        engine.sanitizeSolution(solution);

        assertEquals(List.of(1L, 2L), orderIds(solution.routes().get(0).stops()));
        assertEquals(List.of(3L), orderIds(solution.routes().get(1).stops()));
        assertEquals(1, solution.unassignedOrders().size());
        assertEquals(4L, solution.unassignedOrders().getFirst().order().orderId());
    }

    @Test
    void evaluateSolutionUsesKilogramWeightAgainstVehicleCapacity() {
        PreparedOrderData preparedOrderData = engine.prepareOrders(List.of(
                order(1L, 5000.0, 0.2),
                order(2L, 4000.0, 0.3)
        ));
        RouteState route = routeWithCapacity(
                10.0,
                1.0,
                preparedOrderData.assignableOrders()
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(List.of(route), new ArrayList<>()),
                config()
        );

        assertFalse(engine.isInfeasible(evaluation));
        assertEquals(9.0, evaluation.routeEvaluations().getFirst().totalWeight(), 0.0001);
        assertEquals(0.5, evaluation.routeEvaluations().getFirst().totalVolume(), 0.0001);
    }

    @Test
    void evaluateSolutionRejectsOrdersThatExceedVehicleWeightCapacityInKilogram() {
        PreparedOrderData preparedOrderData = engine.prepareOrders(List.of(
                order(1L, 5000.0, 0.2),
                order(2L, 6000.0, 0.3)
        ));
        RouteState route = routeWithCapacity(
                10.0,
                1.0,
                preparedOrderData.assignableOrders()
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(List.of(route), new ArrayList<>()),
                config()
        );

        assertTrue(engine.isInfeasible(evaluation));
    }

    @Test
    void evaluateSolutionWaitsForPickupWindowAndCalculatesLatenessWhenAllowed() {
        PickupOrderNode firstOrder = orderNodeWithWindow(
                1L,
                PLANNING_START.plusMinutes(30),
                PLANNING_START.plusHours(1)
        );
        PickupOrderNode secondOrder = orderNodeWithWindow(
                2L,
                PLANNING_START,
                PLANNING_START.plusHours(1)
        );
        TravelMetricProvider metricProvider = metricProvider(
                List.of(firstOrder, secondOrder),
                new double[][]{
                        {0.0, 1.0, 0.0},
                        {0.0, 0.0, 2.0},
                        {3.0, 0.0, 0.0}
                },
                new long[][]{
                        {0, 10, 0},
                        {0, 0, 30},
                        {1, 0, 0}
                }
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(
                        List.of(routeWithCapacity(10.0, 1.0, List.of(firstOrder, secondOrder))),
                        new ArrayList<>()
                ),
                config(metricProvider)
        );

        RouteEvaluation routeEvaluation = evaluation.routeEvaluations().getFirst();
        StopEvaluationData firstStop = routeEvaluation.stopDetails().get(0);
        StopEvaluationData secondStop = routeEvaluation.stopDetails().get(1);

        assertFalse(engine.isInfeasible(evaluation));
        assertEquals(6.0, routeEvaluation.totalDistanceKm(), 0.0001);
        assertEquals(41, routeEvaluation.totalTravelMinutes());
        assertEquals(16, routeEvaluation.totalServiceMinutes());
        assertEquals(8, routeEvaluation.totalLatenessMinutes());
        assertEquals(PLANNING_START.plusMinutes(10), firstStop.arrivalTime());
        assertEquals(PLANNING_START.plusMinutes(30), firstStop.startServiceTime());
        assertEquals(PLANNING_START.plusMinutes(68), secondStop.startServiceTime());
        assertEquals(8, secondStop.latenessMinutes());
        assertEquals(PLANNING_START.plusMinutes(77), routeEvaluation.routeEndTime());
    }

    @Test
    void evaluateSolutionRejectsLateStopsWhenLatenessIsNotAllowed() {
        PickupOrderNode firstOrder = orderNodeWithWindow(
                1L,
                PLANNING_START.plusMinutes(30),
                PLANNING_START.plusHours(1)
        );
        PickupOrderNode secondOrder = orderNodeWithWindow(
                2L,
                PLANNING_START,
                PLANNING_START.plusHours(1)
        );
        TravelMetricProvider metricProvider = metricProvider(
                List.of(firstOrder, secondOrder),
                new double[][]{
                        {0.0, 1.0, 0.0},
                        {0.0, 0.0, 2.0},
                        {3.0, 0.0, 0.0}
                },
                new long[][]{
                        {0, 10, 0},
                        {0, 0, 30},
                        {1, 0, 0}
                }
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(
                        List.of(routeWithCapacity(10.0, 1.0, List.of(firstOrder, secondOrder))),
                        new ArrayList<>()
                ),
                config(metricProvider, false, false, true)
        );

        assertTrue(engine.isInfeasible(evaluation));
    }

    @Test
    void evaluateSolutionRejectsRoutesThatReturnAfterPlanningEndWhenEnforced() {
        PickupOrderNode order = orderNode(1L);
        TravelMetricProvider metricProvider = metricProvider(
                List.of(order),
                new double[][]{
                        {0.0, 1.0},
                        {1.0, 0.0}
                },
                new long[][]{
                        {0, 10},
                        {10, 0}
                }
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(
                        List.of(routeWithCapacity(10.0, 1.0, List.of(order))),
                        new ArrayList<>()
                ),
                config(
                        metricProvider,
                        true,
                        true,
                        true,
                        PLANNING_START,
                        PLANNING_START.plusMinutes(20),
                        8,
                        1.0,
                        0.5,
                        500.0,
                        3.0,
                        120
                )
        );

        assertTrue(engine.isInfeasible(evaluation));
    }

    @Test
    void evaluateSolutionRejectsRoutesWithoutVehicle() {
        PickupOrderNode order = orderNode(1L);

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(
                        List.of(route(10L, null, 20, 10.0, 1.0, List.of(order))),
                        new ArrayList<>()
                ),
                config()
        );

        assertTrue(engine.isInfeasible(evaluation));
    }

    @Test
    void evaluateSolutionIncludesDistanceUnassignedAndUsedRoutePenaltiesInObjective() {
        PickupOrderNode assignedOrder = orderNode(1L);
        PickupOrderNode unassignedOrder = orderNode(2L);
        TravelMetricProvider metricProvider = metricProvider(
                List.of(assignedOrder),
                new double[][]{
                        {0.0, 1.0},
                        {1.0, 0.0}
                },
                new long[][]{
                        {0, 1},
                        {1, 0}
                }
        );

        SolutionEvaluation evaluation = engine.evaluateSolution(
                new SolutionState(
                        List.of(routeWithCapacity(10.0, 1.0, List.of(assignedOrder))),
                        new ArrayList<>(List.of(new UnassignedOrderState(unassignedOrder, "UNASSIGNED", true)))
                ),
                config(
                        metricProvider,
                        true,
                        false,
                        true,
                        PLANNING_START,
                        PLANNING_END,
                        8,
                        2.0,
                        0.5,
                        100.0,
                        7.0,
                        120
                )
        );

        assertFalse(engine.isInfeasible(evaluation));
        assertEquals(2.0, evaluation.totalDistanceKm(), 0.0001);
        assertEquals(1, evaluation.assignedOrders());
        assertEquals(1, evaluation.unassignedOrders());
        assertEquals(1, evaluation.usedRoutes());
        assertEquals(111.0, evaluation.objectiveScore(), 0.0001);
    }

    private static TmsOrderOperationView order(Long id, Double weightGram, Double volumeM3) {
        double offset = id == null ? 0.0 : id * 0.001;
        return order(
                id,
                weightGram,
                volumeM3,
                10.0 + offset,
                106.0 + offset,
                PLANNING_START,
                PLANNING_END
        );
    }

    private static TmsOrderOperationView order(
            Long id,
            Double weightGram,
            Double volumeM3,
            Double latitude,
            Double longitude,
            LocalDateTime pickupTimeStart,
            LocalDateTime pickupTimeEnd
    ) {
        return TmsOrderOperationView.builder()
                .id(id)
                .orderCode("ORD-" + id)
                .customerOrderCode("CUS-" + id)
                .senderName("Sender " + id)
                .senderPhone("090000000" + id)
                .senderLatitude(latitude)
                .senderLongitude(longitude)
                .totalWeight(weightGram)
                .totalVolume(volumeM3)
                .pickupTimeStart(pickupTimeStart)
                .pickupTimeEnd(pickupTimeEnd)
                .build();
    }

    private static PickupOrderNode orderNode(Long id) {
        return orderNode(id, 1.0, 0.1);
    }

    private static PickupOrderNode orderNode(Long id, double weightKg, double volumeM3) {
        double offset = id == null ? 0.0 : id * 0.001;
        return new PickupOrderNode(
                id,
                "ORD-" + id,
                "CUS-" + id,
                "Sender " + id,
                "090000000" + id,
                10.0 + offset,
                106.0 + offset,
                weightKg,
                volumeM3,
                PLANNING_START,
                PLANNING_END
        );
    }

    private static PickupOrderNode orderNodeWithWindow(
            Long id,
            LocalDateTime pickupTimeStart,
            LocalDateTime pickupTimeEnd
    ) {
        double offset = id == null ? 0.0 : id * 0.001;
        return new PickupOrderNode(
                id,
                "ORD-" + id,
                "CUS-" + id,
                "Sender " + id,
                "090000000" + id,
                10.0 + offset,
                106.0 + offset,
                1.0,
                0.1,
                pickupTimeStart,
                pickupTimeEnd
        );
    }

    private static PickupOrderNode orderNodeWithoutLocation(Long id) {
        return new PickupOrderNode(
                id,
                "ORD-" + id,
                "CUS-" + id,
                "Sender " + id,
                "090000000" + id,
                null,
                null,
                1.0,
                0.1,
                PLANNING_START,
                PLANNING_END
        );
    }

    private static RouteState routeWithCapacity(
            double maxWeightKg,
            double maxVolumeM3,
            List<PickupOrderNode> stops
    ) {
        return route(10L, 100L, 20, maxWeightKg, maxVolumeM3, stops);
    }

    private static RouteState route(
            Long courierStaffId,
            Long vehicleId,
            Integer maxStops,
            double maxWeightKg,
            double maxVolumeM3,
            List<PickupOrderNode> stops
    ) {
        return new RouteState(
                courierStaffId,
                "CR-" + courierStaffId,
                "Courier " + courierStaffId,
                maxStops,
                vehicleId,
                vehicleId == null ? null : "51A-" + vehicleId,
                maxWeightKg,
                maxVolumeM3,
                10.0,
                106.0,
                new ArrayList<>(stops)
        );
    }

    private static AlgorithmConfig config() {
        return config(null);
    }

    private static AlgorithmConfig config(TravelMetricProvider travelMetricProvider) {
        return config(travelMetricProvider, true, false, true);
    }

    private static AlgorithmConfig config(
            TravelMetricProvider travelMetricProvider,
            boolean allowLateness,
            boolean enforcePlanningEnd,
            boolean enforceCapacity
    ) {
        return config(
                travelMetricProvider,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                PLANNING_START,
                PLANNING_END,
                8,
                1.0,
                0.5,
                500.0,
                3.0,
                120
        );
    }

    private static AlgorithmConfig configWithMatrixMaxNodes(int distanceMatrixMaxNodes) {
        return config(
                null,
                true,
                false,
                true,
                PLANNING_START,
                PLANNING_END,
                8,
                1.0,
                0.5,
                500.0,
                3.0,
                distanceMatrixMaxNodes
        );
    }

    private static AlgorithmConfig config(
            TravelMetricProvider travelMetricProvider,
            boolean allowLateness,
            boolean enforcePlanningEnd,
            boolean enforceCapacity,
            LocalDateTime planningStartTime,
            LocalDateTime planningEndTime,
            int serviceMinutesPerStop,
            double distanceWeight,
            double latenessWeight,
            double unassignedPenalty,
            double usedRoutePenalty,
            int distanceMatrixMaxNodes
    ) {
        return new AlgorithmConfig(
                planningStartTime,
                planningEndTime,
                LocalDate.from(planningStartTime),
                20,
                25.0,
                serviceMinutesPerStop,
                allowLateness,
                enforcePlanningEnd,
                enforceCapacity,
                distanceWeight,
                latenessWeight,
                unassignedPenalty,
                usedRoutePenalty,
                RoutingVehicle.BIKE,
                20,
                distanceMatrixMaxNodes,
                travelMetricProvider
        );
    }

    private static TravelMetricProvider simpleMetricProvider(List<PickupOrderNode> orders) {
        int nodeCount = orders.size() + 1;
        double[][] distanceKm = new double[nodeCount][nodeCount];
        long[][] travelMinutes = new long[nodeCount][nodeCount];
        for (int fromIndex = 0; fromIndex < nodeCount; fromIndex++) {
            for (int toIndex = 0; toIndex < nodeCount; toIndex++) {
                if (fromIndex == toIndex) {
                    continue;
                }
                distanceKm[fromIndex][toIndex] = Math.abs(fromIndex - toIndex) + 1.0;
                travelMinutes[fromIndex][toIndex] = Math.abs(fromIndex - toIndex) + 1L;
            }
        }
        return metricProvider(orders, distanceKm, travelMinutes);
    }

    private static TravelMetricProvider metricProvider(
            List<PickupOrderNode> orders,
            double[][] distanceKm,
            long[][] travelMinutes
    ) {
        Map<Long, Integer> orderNodeIndexByOrderId = new HashMap<>();
        List<NodePoint> nodes = new ArrayList<>();
        nodes.add(new NodePoint(null, 10.0, 106.0));
        for (PickupOrderNode order : orders) {
            int index = nodes.size();
            orderNodeIndexByOrderId.put(order.orderId(), index);
            nodes.add(new NodePoint(order.orderId(), order.latitude(), order.longitude()));
        }
        return new TravelMetricProvider(orderNodeIndexByOrderId, nodes, distanceKm, travelMinutes);
    }

    private static List<Long> orderIds(List<PickupOrderNode> orders) {
        return orders.stream().map(PickupOrderNode::orderId).toList();
    }

    private static final class RecordingDistanceMatrixCaller implements DistanceMatrixCaller {
        private final DistanceMatrixResult result;
        private int calls;
        private List<GeoPoint> lastOrigins = List.of();
        private List<GeoPoint> lastDestinations = List.of();
        private RoutingVehicle lastVehicle;

        private RecordingDistanceMatrixCaller(DistanceMatrixResult result) {
            this.result = result;
        }

        @Override
        public DistanceMatrixResult calculateDistanceMatrix(
                List<GeoPoint> origins,
                List<GeoPoint> destinations,
                RoutingVehicle vehicle
        ) {
            calls++;
            lastOrigins = List.copyOf(origins);
            lastDestinations = List.copyOf(destinations);
            lastVehicle = vehicle;
            return result;
        }
    }
}
