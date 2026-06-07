package serp.project.payment_service.dto.transaction;

import serp.project.payment_service.entity.ZaloPayTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Request DTO cho việc lọc lịch sử giao dịch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tham số lọc lịch sử giao dịch")
public class TransactionHistoryFilterRequest {
    
    @Schema(description = "Username của người dùng (chỉ dành cho admin xem giao dịch của user cụ thể)", example = "user123")
    private String appUser;
    
    @Schema(description = "Trạng thái giao dịch", example = "SUCCESS", allowableValues = {"PENDING", "SUCCESS", "FAILED", "EXPIRED"})
    private ZaloPayTransaction.TransactionStatus status;
    
    @Schema(description = "Thời gian bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-01-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @Schema(description = "Thời gian kết thúc (format: yyyy-MM-dd'T'HH:mm:ss)", example = "2024-12-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    @Schema(description = "Số tiền tối thiểu (VNĐ)", example = "10000")
    private Long minAmount;
    
    @Schema(description = "Số tiền tối đa (VNĐ)", example = "10000000")
    private Long maxAmount;
    
    @Schema(description = "Mã ngân hàng", example = "970436")
    private String bankCode;
    
    @Schema(description = "Từ khóa tìm kiếm trong mô tả hoặc tiêu đề", example = "đơn hàng")
    private String searchKeyword;
    
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0", defaultValue = "0")
    private Integer page = 0;
    
    @Schema(description = "Số lượng bản ghi mỗi trang", example = "20", defaultValue = "20")
    private Integer size = 20;
    
    @Schema(description = "Trường sắp xếp", example = "createdAt", defaultValue = "createdAt", 
            allowableValues = {"createdAt", "amount", "status", "paidAt"})
    private String sortBy = "createdAt";
    
    @Schema(description = "Hướng sắp xếp", example = "DESC", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}
