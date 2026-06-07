package serp.project.payment_service.scheduler;

import serp.project.payment_service.dto.zalopay.QueryOrderRequest;
import serp.project.payment_service.dto.zalopay.QueryOrderResponse;
import serp.project.payment_service.entity.ZaloPayTransaction;
import serp.project.payment_service.repository.ZaloPayTransactionRepository;
import serp.project.payment_service.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler để tự động query trạng thái đơn hàng pending
 * Chạy mỗi phút để check các đơn hàng chưa nhận được callback
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusQueryScheduler {
    
    private final ZaloPayTransactionRepository transactionRepository;
    private final ZaloPayService zaloPayService;
    
    /**
     * Tự động query trạng thái các đơn hàng PENDING
     * Chạy mỗi 1 phút theo best practice của ZaloPay
     */
    @Scheduled(fixedDelay = 60000) // 60 seconds = 1 minute
    public void queryPendingOrders() {
        try {
            // Tìm các transaction PENDING được tạo trong vòng 15 phút (thời gian expire mặc định)
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            
            List<ZaloPayTransaction> pendingTransactions = transactionRepository
                    .findPendingTransactionsAfter(fifteenMinutesAgo);
            
            if (pendingTransactions.isEmpty()) {
                log.debug("No pending transactions to query");
                return;
            }
            
            log.info("Found {} pending transactions to query", pendingTransactions.size());
            
            for (ZaloPayTransaction transaction : pendingTransactions) {
                try {
                    queryAndUpdateTransaction(transaction);
                } catch (Exception e) {
                    log.error("Error querying transaction {}: {}", 
                            transaction.getAppTransId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error in order status query scheduler: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Query và update một transaction
     */
    private void queryAndUpdateTransaction(ZaloPayTransaction transaction) {
        String appTransId = transaction.getAppTransId();
        
        log.info("Querying status for transaction: {}", appTransId);
        
        QueryOrderRequest request = QueryOrderRequest.builder()
                .appTransId(appTransId)
                .build();
        
        QueryOrderResponse response = zaloPayService.queryOrderStatus(request);
        
        // Log kết quả
        log.info("Query result for {}: status={}, message={}", 
                appTransId, response.getStatus(), response.getMessage());
        
        // Reload transaction để lấy status mới nhất sau khi queryOrderStatus đã update
        ZaloPayTransaction updatedTransaction = transactionRepository
                .findByAppTransId(appTransId)
                .orElse(null);
        
        if (updatedTransaction == null) {
            log.warn("Transaction not found after query: {}", appTransId);
            return;
        }
        
        // Nếu đơn hàng vẫn PENDING sau khi query và đã expired, update status
        if (updatedTransaction.getStatus() == ZaloPayTransaction.TransactionStatus.PENDING 
                && isExpired(updatedTransaction)) {
            updateTransactionToExpired(updatedTransaction);
        }
    }
    
    /**
     * Kiểm tra transaction đã hết hạn chưa
     */
    private boolean isExpired(ZaloPayTransaction transaction) {
        // Mặc định thời gian hết hạn là 15 phút
        LocalDateTime expireTime = transaction.getCreatedAt().plusMinutes(15);
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * Update transaction sang trạng thái EXPIRED và gửi thông báo
     */
    private void updateTransactionToExpired(ZaloPayTransaction transaction) {
        try {
            transaction.setStatus(ZaloPayTransaction.TransactionStatus.EXPIRED);
            transactionRepository.save(transaction);
            log.info("Updated transaction {} to EXPIRED", transaction.getAppTransId());

            // Gửi email thông báo giao dịch hết hạn
            zaloPayService.sendPaymentFailedNotification(
                    transaction,
                    "Giao dịch đã hết hạn thanh toán (quá 15 phút)"
            );
        } catch (Exception e) {
            log.error("Error updating transaction to expired: {}", e.getMessage(), e);
        }
    }

    /**
     * Job chạy định kỳ để hủy các đơn hàng đã quá hạn 15 phút.
     * Chạy mỗi 5 phút một lần (không cần chạy quá dày đặc như job query).
     */
    @Scheduled(cron = "0 */5 * * * *") // Chạy vào phút thứ 0, 5, 10, 15... mỗi giờ
    public void scanAndExpireOverdueOrders() {
        log.info("Starting job to expire overdue transactions...");
        try {
            // Xác định mốc thời gian: Tất cả đơn tạo trước thời điểm này đều bị coi là quá hạn
            // Thêm 1 phút buffer (16 phút) để tránh xung đột với các đơn vừa chạm mốc 15p đang được query
            LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(16);

            // Lấy danh sách các đơn "treo" quá lâu
            List<ZaloPayTransaction> overdueTransactions = transactionRepository
                    .findPendingTransactionsBefore(expirationThreshold);

            if (overdueTransactions.isEmpty()) {
                log.info("No overdue pending transactions found.");
                return;
            }

            log.info("Found {} overdue transactions to expire.", overdueTransactions.size());

            for (ZaloPayTransaction transaction : overdueTransactions) {
                try {
                    // Logic xử lý hủy đơn
                    processExpireTransaction(transaction);
                } catch (Exception e) {
                    log.error("Failed to expire transaction {}: {}", transaction.getAppTransId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in expiration scheduler: ", e);
        }
    }

    /**
     * Logic chi tiết hủy đơn
     */
    private void processExpireTransaction(ZaloPayTransaction transaction) {
        // [QUAN TRỌNG] Nên query ZaloPay lần cuối cùng (Final Check) để chắc chắn khách không vừa thanh toán xong
        // Nếu muốn nhanh, bỏ qua bước này. Nhưng an toàn thì nên giữ.
        try {
            QueryOrderResponse response = zaloPayService.queryOrderStatus(
                    QueryOrderRequest.builder().appTransId(transaction.getAppTransId()).build()
            );

            // Nếu ZaloPay bảo đã thành công rồi -> Update Success chứ KHÔNG hủy
            if (response.getStatus().equals("SUCCESS")) {
                log.info("Transaction {} actually succeeded at last minute. Updating to SUCCESS.", transaction.getAppTransId());
                // updateTransactionToSuccess(transaction); // Gọi hàm update thành công của bạn
                return;
            }
        } catch (Exception ex) {
            log.warn("Could not perform final check for {}, proceeding to expire.", transaction.getAppTransId());
        }

        // Tiến hành hủy đơn
        transaction.setStatus(ZaloPayTransaction.TransactionStatus.EXPIRED);
        transaction.setReturnMessage("System auto-expired after 15 minutes"); // Nếu có field này
        transactionRepository.save(transaction);

        log.info("Auto-expired transaction: {}", transaction.getAppTransId());

        // Gửi thông báo hoặc hoàn tồn kho (Rollback Inventory) nếu cần
        // inventoryService.releaseStock(transaction.getOrderId());
    }
}
