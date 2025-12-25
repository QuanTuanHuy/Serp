package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class OrderItemUpdateForm {

    @Min(value = 1, message = "orderItemSeqId must be at least 1")
    private int orderItemSeqId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    @Min(value = 0, message = "unitPrice must be non-negative")
    private float tax;

    @Min(value = 0, message = "unitPrice must be non-negative")
    private long discount;

}
