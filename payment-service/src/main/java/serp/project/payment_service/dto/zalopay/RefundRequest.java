package serp.project.payment_service.dto.zalopay;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu hoàn tiền ZaloPay từ client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    
    /**
     * Mã giao dịch Zalopay muốn hoàn tiền (zp_trans_id)
     * Merchant nhận được từ callback hoặc API get order's status
     */
    @NotBlank(message = "zp_trans_id is required")
    private String zpTransId;
    
    /**
     * Số tiền cần hoàn tiền
     */
    @NotNull(message = "amount is required")
    @Min(value = 1000, message = "amount must be at least 1000 VND")
    private Long amount;
    
    /**
     * Phí của giao dịch hoàn tiền (optional)
     * Phí này sẽ được trích ra từ phần tiền người mua hàng nhận lại
     * Nếu không cần phí hoàn, có thể bỏ qua trường này
     */
    private Long refundFeeAmount;
    
    /**
     * Lý do hoàn tiền
     */
    @NotBlank(message = "description is required")
    private String description;
}
