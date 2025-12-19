package serp.project.sales.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.sales.constant.InventoryItemStatus;
import serp.project.sales.validator.EnumValidator;

import java.time.LocalDate;

@Data
public class InventoryItemUpdateForm {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;

    @EnumValidator(enumClass = InventoryItemStatus.class)
    private String statusId;

}
