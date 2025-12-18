package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private int priority;
    private String saleChannelId;

    @NotEmpty(message = "The list must contain at least one item")
    private List<OrderItem> orderItems;

    @Data
    public static class OrderItem {
        @NotBlank(message = "productId is mandatory")
        private String productId;
        private int orderItemSeqId;

        @NotNull(message = "quantity is mandatory")
        private int quantity;
        private float tax;
        private long discount;
    }

}
