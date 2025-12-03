package serp.project.logistics.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InventoryItemCreationForm {

    private String productId;
    private int quantity;
    private String facilityId;
    private LocalDate expirationDate;
    private LocalDate manufacturingDate;
    private String statusId;

}
