package serp.project.sales.dto.request;

import lombok.Data;
import serp.project.sales.constant.SaleChannel;
import serp.project.sales.validator.EnumValidator;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;

@Data
public class OrderUpdateForm {

    private LocalDate deliveryBeforeDate;
    private LocalDate deliveryAfterDate;
    private String note;
    private String orderName;

    @Min(value = 0, message = "priority must be non-negative")
    private int priority;

    @EnumValidator(enumClass = SaleChannel.class)
    private String saleChannelId;

}
