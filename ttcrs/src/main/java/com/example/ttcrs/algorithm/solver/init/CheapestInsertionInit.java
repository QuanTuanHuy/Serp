package com.example.ttcrs.algorithm.solver.init;

import java.util.ArrayList;
import java.util.List;

import com.example.ttcrs.algorithm.solver.TruckContainerSolver;
import com.example.ttcrs.algorithm.vrp.Constants;
import com.example.ttcrs.algorithm.vrp.entities.Point;


public class CheapestInsertionInit implements InitializationStrategy {
    @Override
    public void initialize(TruckContainerSolver solver) {
        // Bước 1: Tính "độ khó" của từng request
        // = (latestAllowedArrivalTime - earliestAllowedArrivalTime) của pickup
        // Request có time window hẹp hơn phải được chèn trước
        List<Integer> sortedIndices = sortRequestsByDifficulty(solver);

        FPIUSInit helper = new FPIUSInit(); // tái sử dụng insertMooc*, removeMooc*

        for (int i : sortedIndices) {
            Point pickup   = solver.pickupPoints.get(i);
            Point delivery = solver.deliveryPoints.get(i);
            int groupId    = solver.point2Group.get(pickup);

            if (solver.XR.route(pickup) != Constants.NULL_POINT
                    || solver.group2marked.get(groupId) == 1)
                continue;

            // Bước 2: Tìm vị trí chèn (p, q) có delta-cost nhỏ nhất
            double bestDelta   = Double.MAX_VALUE;
            Point  bestPrePick = null, bestPreDel = null;
            int    bestRoute   = -1;

            for (int r = 1; r <= solver.XR.getNbRoutes(); r++) {
                double baseCost = solver.objective.getValue();
                Point st = solver.XR.getStartingPointOfRoute(r);
                int groupTruck = solver.point2Group.get(st);
                if (solver.group2marked.get(groupTruck) == 1
                        && solver.XR.index(solver.XR.getTerminatingPointOfRoute(r)) <= 1)
                    continue;

                for (Point p = st; p != solver.XR.getTerminatingPointOfRoute(r); p = solver.XR.next(p)) {
                    for (Point q = p; q != solver.XR.getTerminatingPointOfRoute(r); q = solver.XR.next(q)) {
                        solver.mgr.performAddTwoPoints(pickup, p, delivery, q);
                        helper.insertMoocToRoutes(solver, r);
                        if (solver.S.violations() == 0) {
                            double delta = solver.objective.getValue() - baseCost;
                            if (delta < bestDelta) {
                                bestDelta   = delta;
                                bestPrePick = p;
                                bestPreDel  = q;
                                bestRoute   = r;
                            }
                        }
                        solver.mgr.performRemoveTwoPoints(pickup, delivery);
                        helper.removeMoocOnRoutes(solver, r);
                    }
                }
            }

            // Bước 3: Commit vị trí tốt nhất
            if (bestPrePick != null) {
                solver.mgr.performAddTwoPoints(pickup, bestPrePick, delivery, bestPreDel);
                Point st = solver.XR.getStartingPointOfRoute(bestRoute);
                solver.group2marked.put(solver.point2Group.get(st), 1);
                solver.group2marked.put(groupId, 1);
            } else {
                solver.rejectPickupPoints.add(pickup);
                solver.rejectDeliveryPoints.add(delivery);
            }
        }
        helper.insertMoocForAllRoutes(solver);
    }

    private List<Integer> sortRequestsByDifficulty(TruckContainerSolver solver) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < solver.pickupPoints.size(); i++) idx.add(i);

        idx.sort((a, b) -> {
            Point paPickup = solver.pickupPoints.get(a);
            Point paDelivery = solver.deliveryPoints.get(a);
            Point pbPickup = solver.pickupPoints.get(b);
            Point pbDelivery = solver.deliveryPoints.get(b);

            double twA = Math.min(
                solver.lastestAllowedArrivalTime.get(paPickup)
                    - solver.earliestAllowedArrivalTime.get(paPickup),
                solver.lastestAllowedArrivalTime.get(paDelivery)
                    - solver.earliestAllowedArrivalTime.get(paDelivery));
            double twB = Math.min(
                solver.lastestAllowedArrivalTime.get(pbPickup)
                    - solver.earliestAllowedArrivalTime.get(pbPickup),
                solver.lastestAllowedArrivalTime.get(pbDelivery)
                    - solver.earliestAllowedArrivalTime.get(pbDelivery));
            return Double.compare(twA, twB);
        });
        return idx;
    }
}
