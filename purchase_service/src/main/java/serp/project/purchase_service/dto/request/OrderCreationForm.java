package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderCreationForm {

    private String fromSupplierId;
    private LocalDate deliveryBeforeDate;
    private LocalDate deliveryAfterDate;
    private String note;
    private String orderName;
    private int priority;
    private String saleChannelId;
    private List<OrderItem> orderItems;

    @Data
    public static class OrderItem {
        private String productId;
        private int orderItemSeqId;
        private int quantity;
        private float tax;
        private long discount;
    }



}
