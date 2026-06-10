/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.enums.RoutingVehicle;
import serp.project.first_mile.service.dto.AlgorithmConfig;
import serp.project.first_mile.service.dto.PickupOrderNode;
import serp.project.first_mile.service.dto.PreparedOrderData;
import serp.project.first_mile.service.dto.RouteState;
import serp.project.first_mile.service.dto.SolutionEvaluation;
import serp.project.first_mile.service.dto.SolutionState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static TmsOrderOperationView order(Long id, Double weightGram, Double volumeM3) {
        return TmsOrderOperationView.builder()
                .id(id)
                .orderCode("ORD-" + id)
                .senderName("Sender " + id)
                .senderPhone("090000000" + id)
                .senderLatitude(10.0 + id * 0.001)
                .senderLongitude(106.0 + id * 0.001)
                .totalWeight(weightGram)
                .totalVolume(volumeM3)
                .pickupTimeStart(PLANNING_START)
                .pickupTimeEnd(PLANNING_END)
                .build();
    }

    private static RouteState routeWithCapacity(
            double maxWeightKg,
            double maxVolumeM3,
            List<PickupOrderNode> stops
    ) {
        return new RouteState(
                10L,
                "CR-10",
                "Courier 10",
                20,
                100L,
                "51A-00001",
                maxWeightKg,
                maxVolumeM3,
                10.0,
                106.0,
                new ArrayList<>(stops)
        );
    }

    private static AlgorithmConfig config() {
        return new AlgorithmConfig(
                PLANNING_START,
                PLANNING_END,
                LocalDate.from(PLANNING_START),
                20,
                25.0,
                8,
                true,
                false,
                true,
                1.0,
                0.5,
                500.0,
                3.0,
                RoutingVehicle.BIKE,
                20,
                120,
                null
        );
    }
}
