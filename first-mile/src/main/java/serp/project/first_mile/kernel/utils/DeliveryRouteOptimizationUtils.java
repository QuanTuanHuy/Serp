/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.utils;

import serp.project.first_mile.domain.DeliveryStop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Optimize last-mile delivery route using Nearest Neighbor + 2-opt.
 * <p>
 * Complexity:
 * - Nearest Neighbor construction: O(n²)
 * - 2-opt improvement: O(n²) per pass, max O(n²) passes → O(n³) worst case
 * - With n ≤ 50: < 5ms on any modern hardware
 */
public final class DeliveryRouteOptimizationUtils {

    private DeliveryRouteOptimizationUtils() {
    }

    public static <T extends DeliveryStop> List<T> optimize(
            double depotLat, double depotLng, List<T> stops) {

        if (stops == null || stops.isEmpty()) return List.of();
        if (stops.size() == 1) return List.copyOf(stops);

        List<T> route = nearestNeighbor(depotLat, depotLng, new ArrayList<>(stops));
        twoOpt(depotLat, depotLng, route);
        return route;
    }

    // ── STEP 1: Nearest Neighbor Construction ──────────────────────────────
    private static <T extends DeliveryStop> List<T> nearestNeighbor(
            double depotLat, double depotLng, List<T> stops) {

        List<T> route = new ArrayList<>(stops.size());
        Set<T> remaining = new LinkedHashSet<>(stops);
        double curLat = depotLat;
        double curLng = depotLng;

        while (!remaining.isEmpty()) {
            T nearest = null;
            double minDist = Double.MAX_VALUE;
            for (T stop : remaining) {
                double d = HaversineUtils.distanceKm(curLat, curLng,
                        stop.getLat(), stop.getLng());
                if (d < minDist) {
                    minDist = d;
                    nearest = stop;
                }
            }
            route.add(nearest);
            remaining.remove(nearest);
            curLat = nearest.getLat();
            curLng = nearest.getLng();
        }
        return route;
    }

    // ── STEP 2: 2-opt Improvement ──────────────────────────────────────────
    private static <T extends DeliveryStop> void twoOpt(
            double depotLat, double depotLng, List<T> route) {

        int n = route.size();
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 2; j < n; j++) {
                    double gain = twoOptGain(depotLat, depotLng, route, i, j);
                    if (gain > 1e-9) {
                        reverseSegment(route, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
    }

    private static <T extends DeliveryStop> double twoOptGain(
            double depotLat, double depotLng, List<T> route, int i, int j) {

        int n = route.size();
        double prevLat = (i == 0) ? depotLat : route.get(i - 1).getLat();
        double prevLng = (i == 0) ? depotLng : route.get(i - 1).getLng();

        double nextLat = (j + 1 < n) ? route.get(j + 1).getLat() : depotLat;
        double nextLng = (j + 1 < n) ? route.get(j + 1).getLng() : depotLng;

        double oldCost = HaversineUtils.distanceKm(prevLat, prevLng,
                route.get(i).getLat(), route.get(i).getLng())
                + HaversineUtils.distanceKm(route.get(j).getLat(), route.get(j).getLng(),
                nextLat, nextLng);
        double newCost = HaversineUtils.distanceKm(prevLat, prevLng,
                route.get(j).getLat(), route.get(j).getLng())
                + HaversineUtils.distanceKm(route.get(i).getLat(), route.get(i).getLng(),
                nextLat, nextLng);
        return oldCost - newCost;
    }

    private static <T> void reverseSegment(List<T> list, int from, int to) {
        while (from < to) {
            T tmp = list.get(from);
            list.set(from, list.get(to));
            list.set(to, tmp);
            from++;
            to--;
        }
    }
}
