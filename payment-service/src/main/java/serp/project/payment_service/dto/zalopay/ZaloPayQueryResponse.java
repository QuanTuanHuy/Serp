package serp.project.payment_service.dto.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response từ ZaloPay Query API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayQueryResponse {
    
    /**
     * Mã trạng thái:
     * 1 = Thành công
     * 2 = Thất bại
     * 3 = Đơn hàng chưa thanh toán hoặc đang xử lý
     */
    @JsonProperty("return_code")
    private Integer returnCode;
    
    /**
     * Thông tin trạng thái đơn hàng
     */
    @JsonProperty("return_message")
    private String returnMessage;
    
    /**
     * Mã trạng thái chi tiết
     */
    @JsonProperty("sub_return_code")
    private Integer subReturnCode;
    
    /**
     * Thông tin chi tiết trạng thái đơn hàng
     */
    @JsonProperty("sub_return_message")
    private String subReturnMessage;
    
    /**
     * Trạng thái xử lý
     */
    @JsonProperty("is_processing")
    private Boolean isProcessing;
    
    /**
     * Số tiền ứng dụng nhận được (chỉ có ý nghĩa khi thanh toán thành công)
     */
    @JsonProperty("amount")
    private Long amount;
    
    /**
     * Số tiền giảm giá
     */
    @JsonProperty("discount_amount")
    private Long discountAmount;
    
    /**
     * Mã giao dịch của ZaloPay
     */
    @JsonProperty("zp_trans_id")
    private Long zpTransId;
}
