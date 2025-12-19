package serp.project.logistics.dto.request;

import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;

@Data
public class InventoryItemDetailUpdateForm {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String note;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private String facilityId;

}
