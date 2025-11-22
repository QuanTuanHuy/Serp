package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShipmentItemAddForm {
    private String productId;

    private int quantity;

    private String orderItemId;

    private String note;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private String facilityId;

    public ShipmentCreationForm.InventoryItemDetail toInventoryItemDetailForm() {
        ShipmentCreationForm.InventoryItemDetail itemDetail = new ShipmentCreationForm.InventoryItemDetail();
        itemDetail.setProductId(getProductId());
        itemDetail.setQuantity(getQuantity());
        itemDetail.setOrderItemId(getOrderItemId());
        itemDetail.setNote(getNote());
        itemDetail.setLotId(getLotId());
        itemDetail.setExpirationDate(getExpirationDate());
        itemDetail.setManufacturingDate(getManufacturingDate());
        return itemDetail;
    }
}
