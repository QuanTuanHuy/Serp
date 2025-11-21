package serp.project.purchase_service.dto.response;

import lombok.Builder;
import lombok.Data;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShipmentDetailResponse
{

    private String id;
    private String shipmentTypeId;
    private String fromSupplierId;
    private String toCustomerId;
    private LocalDateTime createdStamp;
    private Long createdByUserId;
    private String orderId;
    private LocalDateTime lastUpdatedStamp;
    private String shipmentName;
    private String statusId;
    private Long handledByUserId;
    private String note;
    private LocalDate expectedDeliveryDate;
    private Long userCancelledId;
    private long totalWeight;
    private int totalQuantity;
    private List<InventoryItemDetailEntity> items;

    public static ShipmentDetailResponse fromEntity(
            serp.project.purchase_service.entity.ShipmentEntity shipment,
            List<InventoryItemDetailEntity> items
    ) {
        return ShipmentDetailResponse.builder()
                .id(shipment.getId())
                .shipmentTypeId(shipment.getShipmentTypeId())
                .fromSupplierId(shipment.getFromSupplierId())
                .toCustomerId(shipment.getToCustomerId())
                .createdStamp(shipment.getCreatedStamp())
                .createdByUserId(shipment.getCreatedByUserId())
                .orderId(shipment.getOrderId())
                .lastUpdatedStamp(shipment.getLastUpdatedStamp())
                .shipmentName(shipment.getShipmentName())
                .statusId(shipment.getStatusId())
                .handledByUserId(shipment.getHandledByUserId())
                .note(shipment.getNote())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .userCancelledId(shipment.getUserCancelledId())
                .totalWeight(shipment.getTotalWeight())
                .totalQuantity(shipment.getTotalQuantity())
                .items(items)
                .build();
    }

}
