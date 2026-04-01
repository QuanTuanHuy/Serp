package serp.project.logistics.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OutboundShipmentCreationForm {

    @NotBlank(message = "Order ID cannot be empty")
    private String orderId;

    @NotBlank(message = "Facility ID cannot be empty")
    private String facilityId;

    private String name;

    @NotEmpty(message = "items cannot be empty")
    private List<ItemForm> items;

    @Data
    public static class ItemForm {
        @NotBlank(message = "Inventory Item Detail ID cannot be empty")
        String inventoryItemDetailId;

        @NotBlank(message = "Inventory Item ID cannot be empty")
        String inventoryItemId;

        @NotBlank(message = "Product ID cannot be empty")
        String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity;
    }

}
