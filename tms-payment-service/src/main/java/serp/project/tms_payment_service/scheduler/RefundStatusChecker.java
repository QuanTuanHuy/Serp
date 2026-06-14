package serp.project.tms_payment_service.scheduler;

import serp.project.tms_payment_service.dto.zalopay.QueryRefundRequest;
import serp.project.tms_payment_service.entity.ZaloPayRefundTransaction;
import serp.project.tms_payment_service.repository.ZaloPayRefundTransactionRepository;
import serp.project.tms_payment_service.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job để tự động kiểm tra trạng thái hoàn tiền đang PROCESSING
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundStatusChecker {
    
    private final ZaloPayRefundTransactionRepository refundTransactionRepository;
    private final ZaloPayService zaloPayService;
    
    /**
     * Chạy mỗi 5 phút để kiểm tra các refund transaction đang PROCESSING
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5 phút, delay 1 phút lần đầu
    public void checkPendingRefunds() {
        try {
            log.info("=== Starting refund status check job ===");
            
            // Lấy tất cả refund transactions đang PROCESSING
            List<ZaloPayRefundTransaction> processingRefunds = refundTransactionRepository
                    .findByStatus(ZaloPayRefundTransaction.RefundStatus.PROCESSING);
            
            if (processingRefunds.isEmpty()) {
                log.info("No PROCESSING refund transactions found");
                return;
            }
            
            log.info("Found {} PROCESSING refund transactions to check", processingRefunds.size());
            
            int successCount = 0;
            int failedCount = 0;
            int stillProcessingCount = 0;
            
            // Kiểm tra từng refund transaction
            for (ZaloPayRefundTransaction refund : processingRefunds) {
                try {
                    log.info("Checking refund status for MRefundId: {}", refund.getMRefundId());
                    
                    // Tạo request để query status
                    QueryRefundRequest request = QueryRefundRequest.builder()
                            .mRefundId(refund.getMRefundId())
                            .build();
                    
                    // Query status từ ZaloPay
                    // Method queryRefundStatus sẽ tự động:
                    // 1. Gọi ZaloPay API
                    // 2. Cập nhật database
                    // 3. Gửi email notification nếu status thay đổi
                    var response = zaloPayService.queryRefundStatus(request);
                    
                    // Đếm số lượng theo trạng thái
                    switch (response.getStatus()) {
                        case "SUCCESS" -> successCount++;
                        case "FAILED" -> failedCount++;
                        case "PROCESSING", "PENDING" -> stillProcessingCount++;
                    }
                    
                    // Delay nhỏ giữa các requests để tránh rate limit
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    log.error("Error checking refund status for MRefundId: {}. Error: {}", 
                            refund.getMRefundId(), e.getMessage(), e);
                }
            }
            
            log.info("=== Refund status check completed === " +
                    "Total: {}, Success: {}, Failed: {}, Still Processing: {}",
                    processingRefunds.size(), successCount, failedCount, stillProcessingCount);
            
        } catch (Exception e) {
            log.error("Error in refund status checker job: {}", e.getMessage(), e);
        }
    }
}
