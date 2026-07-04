package serp.project.tms_payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBankDTO {
    private String bankCode;
    private String name;
    private Integer displayOrder;
    private Integer pmcId;
    private Long minAmount;
    private Long maxAmount;
}
