package serp.project.logistics.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InventoryItemUpdateForm {

    private int quantity;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;
    private String statusId;

}
