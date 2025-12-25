package serp.project.purchase_service.dto.request;

import lombok.Data;
import serp.project.purchase_service.constant.SaleChannel;
import serp.project.purchase_service.validator.EnumValidator;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderCreationForm {

    @NotBlank(message = "supplierId is mandatory")
    private String fromSupplierId;

    private LocalDate deliveryBeforeDate;
    private LocalDate deliveryAfterDate;
    private String note;
    private String orderName;

    @Min(value = 0, message = "priority must be non-negative")
    private int priority;

    @NotNull(message = "saleChannelId is mandatory")
    @EnumValidator(enumClass = SaleChannel.class)
    private String saleChannelId;

    @NotEmpty(message = "The list must contain at least one item")
    private List<OrderItem> items;

    @Data
    public static class OrderItem {
        @NotBlank(message = "productId is mandatory")
        private String productId;

        @Min(value = 1, message = "orderItemSeqId must be at least 1")
        private int orderItemSeqId;

        @Min(value = 1, message = "quantity must be at least 1")
        private int quantity;

        @Min(value = 0, message = "unitPrice must be non-negative")
        private float tax;

        @Min(value = 0, message = "unitPrice must be non-negative")
        private long discount;
    }

}
