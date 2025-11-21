package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryItemDetailUpdateForm {

    private int quantity;

    private String note;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

}
