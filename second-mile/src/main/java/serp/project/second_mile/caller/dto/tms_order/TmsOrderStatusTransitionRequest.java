/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.caller.dto.tms_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsOrderStatusTransitionRequest {
    private String source;
    private String idempotencyKey;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long orderId;
        private String orderCode;
        private List<OrderStatus> expectedStatuses;
        private OrderStatus targetStatus;
        private String description;
        private Context context;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        private LocalDateTime eventTime;
        private Long hubId;
        private String hubCode;
        private String hubName;
        private String postOfficeCode;
        private Long manifestId;
        private String manifestCode;
        private Long bagId;
        private String bagCode;
        private Long routeId;
        private String routeCode;
        private Long driverId;
        private String driverCode;
        private String driverName;
        private Long vehicleId;
        private String vehicleLicensePlate;
        private Double latitude;
        private Double longitude;
        private String locationLabel;
    }
}
