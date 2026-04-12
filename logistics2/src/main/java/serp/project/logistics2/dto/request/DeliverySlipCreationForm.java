package serp.project.logistics2.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliverySlipCreationForm {

    @NotBlank(message = "Outbound shipment ID is required")
    private String outboundShipmentId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Facility ID is required")
    private String facilityId;

    @NotNull(message = "Time window start is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeWindowStart; // ISO 8601 format

    @NotNull(message = "Time window end is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeWindowEnd; // ISO 8601 format

    @NotEmpty(message = "At least one delivery item is required")
    private List<ItemForm> items;

    @Data
    public static class ItemForm {
        @NotBlank(message = "Outbound shipment item ID is required")
        private String outbountShipmentItemId;

        @NotBlank(message = "Inventory item ID is required")
        private String inventoryItemId;

        @NotBlank(message = "Product ID is required")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

}
