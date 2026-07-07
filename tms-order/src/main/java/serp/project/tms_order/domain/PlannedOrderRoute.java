/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannedOrderRoute {
    private String originPostOfficeCode;
    private String destinationPostOfficeCode;
    private Double totalEstimatedDistanceKm;
    private Integer totalEstimatedDurationMinutes;
    private List<Leg> legs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Leg {
        private Integer sequence;
        private Long routeId;
        private String routeCode;
        private String routeName;
        private String originType;
        private Long originHubId;
        private String originPostOfficeCode;
        private String destinationType;
        private Long destinationHubId;
        private String destinationPostOfficeCode;
        private Long vehicleId;
        private Double estimatedDistanceKm;
        private Integer estimatedDurationMinutes;
    }
}
