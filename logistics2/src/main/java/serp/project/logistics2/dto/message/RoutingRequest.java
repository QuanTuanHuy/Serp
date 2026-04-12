package serp.project.logistics2.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RoutingRequest {
    @JsonProperty("plan_id")
    private String planId;

    private Depot depot;
    private List<Vehicle> vehicles;
    private List<Slip> slips;

    public RoutingRequest(
            String planId,
            Depot depot,
            List<Vehicle> vehicles,
            List<Slip> slips) {
        this.planId = planId;
        this.depot = depot;
        this.vehicles = vehicles;
        this.slips = slips;
    }

    @Data
    public static class Depot {
        @JsonProperty("depot_id")
        private String depotId;
        private double lat;
        private double lng;

        public Depot(
                String depotId,
                double lat,
                double lng) {
            this.depotId = depotId;
            this.lat = lat;
            this.lng = lng;
        }
    }

    @Data
    public static class Vehicle {
        @JsonProperty("vehicle_id")
        private String vehicleId;
        @JsonProperty("max_weight")
        private Long maxWeight;
        @JsonProperty("max_volume")
        private Double maxVolume;

        public Vehicle(
                String vehicleId,
                Long maxWeight,
                Double maxVolume) {
            this.vehicleId = vehicleId;
            this.maxWeight = maxWeight;
            this.maxVolume = maxVolume;
        }
    }

    @Data
    public static class Slip {
        @JsonProperty("slip_id")
        private String slipId;
        private float lat;
        private float lng;
        private Long weight;
        private Double volume;

        public Slip(
                String slipId,
                float lat,
                float lng,
                Long weight,
                Double volume) {
            this.slipId = slipId;
            this.lat = lat;
            this.lng = lng;
            this.weight = weight;
            this.volume = volume;
        }
    }
}