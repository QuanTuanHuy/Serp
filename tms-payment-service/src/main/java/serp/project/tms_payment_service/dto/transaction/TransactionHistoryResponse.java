package serp.project.tms_payment_service.dto.transaction;

import serp.project.tms_payment_service.entity.ZaloPayTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho lịch sử giao dịch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa lịch sử giao dịch với phân trang")
public class TransactionHistoryResponse {
    
    @Schema(description = "Danh sách giao dịch")
    private List<TransactionDetailDTO> transactions;
    
    @Schema(description = "Tổng số giao dịch")
    private Long totalElements;
    
    @Schema(description = "Tổng số trang")
    private Integer totalPages;
    
    @Schema(description = "Trang hiện tại")
    private Integer currentPage;
    
    @Schema(description = "Số lượng bản ghi mỗi trang")
    private Integer pageSize;
    
    @Schema(description = "Có trang tiếp theo không")
    private Boolean hasNext;
    
    @Schema(description = "Có trang trước không")
    private Boolean hasPrevious;
    
    /**
     * DTO chi tiết giao dịch
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin chi tiết một giao dịch")
    public static class TransactionDetailDTO {
        
        @Schema(description = "ID giao dịch", example = "12345")
        private Long id;
        
        @Schema(description = "Mã giao dịch nội bộ", example = "241125_1732523400000")
        private String appTransId;
        
        @Schema(description = "Mã giao dịch ZaloPay", example = "1234567890")
        private Long zpTransId;
        
        @Schema(description = "Username người dùng", example = "user123")
        private String appUser;
        
        @Schema(description = "Số tiền giao dịch (VNĐ)", example = "500000")
        private Long amount;
        
        @Schema(description = "Mô tả giao dịch", example = "Thanh toán đơn hàng #ORD123")
        private String description;
        
        @Schema(description = "Tiêu đề giao dịch", example = "Đơn hàng #ORD123")
        private String title;
        
        @Schema(description = "Mã ngân hàng", example = "970436")
        private String bankCode;
        
        @Schema(description = "Trạng thái giao dịch", example = "SUCCESS")
        private ZaloPayTransaction.TransactionStatus status;
        
        @Schema(description = "Mã trả về từ ZaloPay", example = "1")
        private Integer returnCode;
        
        @Schema(description = "Thông báo trả về từ ZaloPay", example = "Giao dịch thành công")
        private String returnMessage;
        
        @Schema(description = "Mã lỗi chi tiết từ ZaloPay")
        private Integer subReturnCode;
        
        @Schema(description = "Thông báo lỗi chi tiết từ ZaloPay")
        private String subReturnMessage;
        
        @Schema(description = "Phí người dùng chịu (VNĐ)", example = "0")
        private Long userFeeAmount;
        
        @Schema(description = "Số tiền được giảm giá (VNĐ)", example = "0")
        private Long discountAmount;
        
        @Schema(description = "Thời gian tạo giao dịch", example = "2024-11-25T10:30:00")
        private LocalDateTime createdAt;
        
        @Schema(description = "Thời gian cập nhật giao dịch", example = "2024-11-25T10:35:00")
        private LocalDateTime updatedAt;
        
        @Schema(description = "Thời gian thanh toán thành công", example = "2024-11-25T10:35:00")
        private LocalDateTime paidAt;
        
        @Schema(description = "Email người dùng", example = "user@example.com")
        private String email;
        
        /**
         * Convert từ Entity sang DTO
         */
        public static TransactionDetailDTO fromEntity(ZaloPayTransaction transaction) {
            return TransactionDetailDTO.builder()
                    .id(transaction.getId())
                    .appTransId(transaction.getAppTransId())
                    .zpTransId(transaction.getZpTransId())
                    .appUser(transaction.getAppUser())
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .title(transaction.getTitle())
                    .bankCode(transaction.getBankCode())
                    .status(transaction.getStatus())
                    .returnCode(transaction.getReturnCode())
                    .returnMessage(transaction.getReturnMessage())
                    .subReturnCode(transaction.getSubReturnCode())
                    .subReturnMessage(transaction.getSubReturnMessage())
                    .userFeeAmount(transaction.getUserFeeAmount())
                    .discountAmount(transaction.getDiscountAmount())
                    .createdAt(transaction.getCreatedAt())
                    .updatedAt(transaction.getUpdatedAt())
                    .paidAt(transaction.getPaidAt())
                    .email(transaction.getEmail())
                    .build();
        }
    }
}
