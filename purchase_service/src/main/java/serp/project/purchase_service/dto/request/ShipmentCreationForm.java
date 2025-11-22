package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ShipmentCreationForm {

    private String fromSupplierId;

    private String orderId;

    private String shipmentName;

    private String note;

    private LocalDate expectedDeliveryDate;

    private List<InventoryItemDetail> items;

    private String facilityId;

    @Data
    public static class InventoryItemDetail {
        private String productId;

        private int quantity;

        private String orderItemId;

        private String note;

        private String lotId;

        private LocalDate expirationDate;

        private LocalDate manufacturingDate;

    }

}
