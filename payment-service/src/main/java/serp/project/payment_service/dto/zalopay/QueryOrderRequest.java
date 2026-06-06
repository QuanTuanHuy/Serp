package serp.project.payment_service.dto.zalopay;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request để query trạng thái đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryOrderRequest {
    
    /**
     * Mã giao dịch nội bộ cần query
     */
    @NotBlank(message = "App trans ID không được để trống")
    private String appTransId;
}
