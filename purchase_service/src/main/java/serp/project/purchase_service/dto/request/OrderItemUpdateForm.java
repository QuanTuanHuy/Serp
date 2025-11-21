package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class OrderItemUpdateForm {

    private int orderItemSeqId;
    private int quantity;
    private float tax;
    private long discount;

}
