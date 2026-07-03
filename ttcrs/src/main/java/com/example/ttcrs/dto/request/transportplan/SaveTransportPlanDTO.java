package com.example.ttcrs.dto.request.transportplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaveTransportPlanDTO {

    @NotEmpty(message = "plans must not be empty")
    @Valid
    private List<PlanItemDTO> plans;

    @Data
    public static class PlanItemDTO {

        @jakarta.validation.constraints.NotBlank(message = "truckCode is required")
        private String truckCode;

        /** Nullable — driver userId from Account service */
        private Long driverId;

        @NotEmpty(message = "stops must not be empty")
        @Valid
        private List<StopDTO> stops;
    }

    @Data
    public static class StopDTO {

        private int sequence;

        @jakarta.validation.constraints.NotBlank(message = "locationCode is required")
        private String locationCode;

        /** Raw algorithm action string, e.g. START_TRUCK, PICKUP_MOOC, WH_PICKUP_FULLCONT */
        @jakarta.validation.constraints.NotBlank(message = "action is required")
        private String action;

        /** ISO-like datetime string: "yyyy-MM-dd HH:mm:ss", nullable for depot ends */
        private String plannedArrival;

        /** DB entity ID of the linked request; null for depot/mooc stops */
        private Long requestId;

        /** DB entity ID of the trailer linked to this stop (optional) */
        private Long trailerId;

        /** Container code cho stop PICKUP_CONTAINER (dùng để gán cho OE request) */
        private String containerCode;
    }
}
