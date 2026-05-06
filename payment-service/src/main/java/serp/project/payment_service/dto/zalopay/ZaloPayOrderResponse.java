package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response từ ZaloPay API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayOrderResponse {
    
    /**
     * Mã trạng thái: 1 = Thành công, 2 = Thất bại
     */
    @JsonProperty("return_code")
    private Integer returnCode;
    
    /**
     * Mô tả mã trạng thái
     */
    @JsonProperty("return_message")
    private String returnMessage;
    
    /**
     * Mã trạng thái chi tiết
     */
    @JsonProperty("sub_return_code")
    private Integer subReturnCode;
    
    /**
     * Mô tả chi tiết mã trạng thái
     */
    @JsonProperty("sub_return_message")
    private String subReturnMessage;
    
    /**
     * Token đơn hàng ZaloPay
     */
    @JsonProperty("zp_trans_token")
    private String zpTransToken;
    
    /**
     * URL đơn hàng (dùng để tạo QR code hoặc redirect)
     */
    @JsonProperty("order_url")
    private String orderUrl;
    
    /**
     * Token đơn hàng
     */
    @JsonProperty("order_token")
    private String orderToken;
    
    /**
     * Mã QR NAPAS VietQR
     */
    @JsonProperty("qr_code")
    private String qrCode;
}
