package serp.project.tms_payment_service.dto.zalopay;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu truy vấn trạng thái hoàn tiền từ client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRefundRequest {
    
    /**
     * Mã giao dịch hoàn tiền của Merchant (m_refund_id)
     */
    @NotBlank(message = "m_refund_id is required")
    private String mRefundId;
}
