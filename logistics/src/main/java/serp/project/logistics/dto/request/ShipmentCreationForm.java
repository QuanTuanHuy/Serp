package serp.project.logistics.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
public class ShipmentCreationForm {

    @NotBlank(message = "orderId cannot be empty")
    private String orderId;

    private String shipmentName;

    private String note;

    private LocalDate expectedDeliveryDate;

    @NotEmpty(message = "items cannot be empty")
    private List<InventoryItemDetail> items;

    @Data
    public static class InventoryItemDetail {

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @NotBlank(message = "orderItemId cannot be empty")
        private String orderItemId;

        private String note;

        @NotBlank(message = "lotId cannot be empty")
        private String lotId;

        private LocalDate expirationDate;

        private LocalDate manufacturingDate;

        @NotBlank(message = "facilityId cannot be empty")
        private String facilityId;

    }

}
