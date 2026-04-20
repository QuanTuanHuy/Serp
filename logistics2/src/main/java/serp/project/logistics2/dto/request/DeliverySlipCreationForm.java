package serp.project.logistics2.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DeliverySlipCreationForm {

    @NotBlank(message = "Outbound shipment ID is required")
    private String outboundShipmentId;

    @NotBlank(message = "Facility ID is required")
    private String facilityId;

    @NotEmpty(message = "At least one delivery item is required")
    private List<ItemForm> items;

    @Data
    public static class ItemForm {
        @NotBlank(message = "Outbound shipment item ID is required")
        private String outboundShipmentItemId;

        @NotBlank(message = "Inventory item ID is required")
        private String inventoryItemId;

        @NotBlank(message = "Product ID is required")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

}
