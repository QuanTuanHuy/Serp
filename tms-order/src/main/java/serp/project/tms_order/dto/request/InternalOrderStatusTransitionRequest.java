/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.tms_order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalOrderStatusTransitionRequest {
    @NotBlank
    private String source;

    @NotBlank
    private String idempotencyKey;

    @Valid
    @NotEmpty
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
