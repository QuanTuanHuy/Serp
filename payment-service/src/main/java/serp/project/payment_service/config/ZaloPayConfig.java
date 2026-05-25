package serp.project.payment_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zalopay")
public class ZaloPayConfig {
    
    /**
     * App ID được cấp bởi ZaloPay
     */
    private Integer appId;
    
    /**
     * Key1 dùng để tạo MAC (HMAC-SHA256)
     */
    private String key1;
    
    /**
     * Key2 dùng để verify callback từ ZaloPay
     */
    private String key2;
    
    /**
     * Endpoint để tạo đơn hàng
     */
    private String createOrderUrl;
    
    /**
     * Endpoint để query trạng thái đơn hàng
     */
    private String queryOrderUrl;
    
    /**
     * Endpoint để lấy danh sách ngân hàng được hỗ trợ
     */
    private String getBankListUrl;
    
    /**
     * Endpoint để thực hiện hoàn tiền
     */
    private String refundUrl;
    
    /**
     * Endpoint để truy vấn trạng thái hoàn tiền
     */
    private String queryRefundUrl;
    
    /**
     * URL callback nhận kết quả thanh toán từ ZaloPay
     */
    private String callbackUrl;
    
    /**
     * URL redirect sau khi thanh toán thành công
     */
    private String redirectUrl;
    
    /**
     * Thời gian hết hạn đơn hàng (giây) - mặc định 15 phút
     */
    private Long expireDurationSeconds = 900L;
}
