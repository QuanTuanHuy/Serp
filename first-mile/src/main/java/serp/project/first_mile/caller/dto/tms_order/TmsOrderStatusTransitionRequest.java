/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto.tms_order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.OrderStatus;

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
        private Boolean recordTimelineWhenUnchanged;
        private Context context;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        private LocalDateTime eventTime;
        private Long tripId;
        private String tripCode;
        private Long postOfficeId;
        private String postOfficeCode;
        private String postOfficeName;
        private Long staffId;
        private String staffCode;
        private String staffName;
        private Long vehicleId;
        private String vehicleLicensePlate;
        private Double latitude;
        private Double longitude;
        private String locationLabel;
    }
}
