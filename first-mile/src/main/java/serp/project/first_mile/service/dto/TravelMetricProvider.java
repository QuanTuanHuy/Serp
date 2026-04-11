package serp.project.first_mile.service.dto;

import java.util.List;
import java.util.Map;

public record TravelMetricProvider(
        Map<Long, Integer> orderNodeIndexByOrderId,
        List<serp.project.first_mile.service.dto.NodePoint> nodes,
        double[][] distanceKm,
        long[][] travelMinutes
) {
    public int nodeCount() {
        return nodes == null ? 0 : nodes.size();
    }
}
