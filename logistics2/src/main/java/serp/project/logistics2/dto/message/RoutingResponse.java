package serp.project.logistics2.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RoutingResponse {
    @JsonProperty("plan_id")
    private String planId;

    private String status;

    @JsonProperty("total_plan_distance")
    private Float totalPlanDistance;

    private List<RouteResult> routes;

    @JsonProperty("unused_vehicle_ids")
    private List<String> unusedVehicleIds;

    @JsonProperty("dropped_slip_ids")
    private List<String> droppedSlipIds;

    @Data
    public static class RouteResult {
        @JsonProperty("vehicle_id")
        private String vehicleId;

        @JsonProperty("route_distance")
        private Float routeDistance;

        @JsonProperty("total_weight")
        private Long totalWeight;

        @JsonProperty("total_volume")
        private Long totalVolume;

        private List<StopResult> stops;
    }

    @Data
    public static class StopResult {
        @JsonProperty("slip_id")
        private String slipId;

        private int sequence;

        @JsonProperty("encoded_polyline")
        private String encodedPolyline;
    }
}
