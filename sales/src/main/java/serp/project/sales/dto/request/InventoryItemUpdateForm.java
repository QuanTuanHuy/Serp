package serp.project.logistics.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.logistics.constant.InventoryItemStatus;
import serp.project.logistics.validator.EnumValidator;

@Data
public class InventoryItemUpdateForm {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;

    @EnumValidator(enumClass = InventoryItemStatus.class)
    private String statusId;

}
