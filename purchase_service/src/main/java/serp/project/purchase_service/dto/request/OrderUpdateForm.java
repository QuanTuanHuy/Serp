package serp.project.purchase_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderUpdateForm {

    private LocalDate deliveryBeforeDate;
    private LocalDate deliveryAfterDate;
    private String note;
    private String orderName;
    private int priority;
    private String saleChannelId;

}
