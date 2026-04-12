package serp.project.logistics2.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.logistics2.constant.InventoryItemStatus;
import serp.project.logistics2.validator.EnumValidator;

@Data
public class InventoryItemUpdateForm {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;

    @EnumValidator(enumClass = InventoryItemStatus.class)
    private String statusId;

}
