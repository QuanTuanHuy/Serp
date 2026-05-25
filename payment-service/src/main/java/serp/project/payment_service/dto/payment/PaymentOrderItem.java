package serp.project.payment_service.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderItem {

    @NotBlank(message = "Item ID cannot be blank")
    private String itemId;

    @NotBlank(message = "Item name cannot be blank")
    private String itemName;

    @NotNull(message = "Item price cannot be null")
    @Min(value = 0, message = "Item price must be >= 0")
    private Long itemPrice;

    @NotNull(message = "Item quantity cannot be null")
    @Min(value = 1, message = "Item quantity must be >= 1")
    private Integer itemQuantity;
}
