/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.DeliveryManifestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryManifestResponse {
    private Long id;
    private String manifestCode;
    private String postOfficeCode;
    private Long courierId;
    private String courierName;
    private String vehicleId;
    private DeliveryManifestStatus status;
    private LocalDate plannedDate;
    private LocalDateTime plannedDepartureAt;
    private LocalDateTime actualDepartureAt;
    private LocalDateTime actualReturnAt;
    private Integer totalOrders;
    private Integer deliveredCount;
    private Integer failedCount;
    private Long totalCodAmount;
    private Long collectedCodAmount;
    private Long totalShippingFee;
    private Long collectedShippingFee;
    private String note;
    private LocalDateTime createdAt;
    private List<DeliveryManifestOrderResponse> orders;
}
