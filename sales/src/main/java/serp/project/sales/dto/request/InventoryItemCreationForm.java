package serp.project.sales.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.sales.constant.InventoryItemStatus;
import serp.project.sales.validator.EnumValidator;

import java.time.LocalDate;

@Data
public class InventoryItemCreationForm {

    @NotBlank(message = "Product ID cannot be empty")
    private String productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Lot ID cannot be empty")
    private String lotId;

    @NotBlank(message = "Facility ID cannot be empty")
    private String facilityId;

    private LocalDate expirationDate;
    private LocalDate manufacturingDate;

    @NotNull(message = "Status ID cannot be empty")
    @EnumValidator(enumClass = InventoryItemStatus.class)
    private String statusId;

}
