package serp.project.payment_service.dto.zalopay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response trả về cho client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    
    /**
     * Mã giao dịch nội bộ
     */
    private String appTransId;
    
    /**
     * URL để redirect hoặc tạo QR code
     */
    private String orderUrl;
    
    /**
     * Token đơn hàng ZaloPay
     */
    private String zpTransToken;
    
    /**
     * Mã QR NAPAS VietQR
     */
    private String qrCode;
    
    /**
     * Trạng thái tạo đơn: SUCCESS, FAILED
     */
    private String status;
    
    /**
     * Thông báo
     */
    private String message;
    
    /**
     * Mã lỗi chi tiết (nếu có)
     */
    private Integer errorCode;
}
