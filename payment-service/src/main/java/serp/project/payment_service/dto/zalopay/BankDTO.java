package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông tin ngân hàng từ ZaloPay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDTO {
    
    /**
     * Mã ngân hàng
     */
    @JsonProperty("bankcode")
    private String bankCode;
    
    /**
     * Tên ngân hàng
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * Thứ tự sắp xếp
     */
    @JsonProperty("displayorder")
    private Integer displayOrder;
    
    /**
     * Payment method category ID
     * 36 = Visa/Master/JCB
     * 37 = Bank Account
     * 38 = ZaloPay
     * 39 = ATM
     * 41 = Visa/Master Debit
     */
    @JsonProperty("pmcid")
    private Integer pmcId;
    
    /**
     * Số tiền thanh toán tối thiểu
     */
    @JsonProperty("minamount")
    private Long minAmount;
    
    /**
     * Số tiền thanh toán tối đa
     */
    @JsonProperty("maxamount")
    private Long maxAmount;
}
