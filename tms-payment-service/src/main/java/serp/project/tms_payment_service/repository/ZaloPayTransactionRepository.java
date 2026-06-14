package serp.project.tms_payment_service.repository;

import serp.project.tms_payment_service.entity.ZaloPayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ZaloPayTransaction
 */
@Repository
public interface ZaloPayTransactionRepository extends JpaRepository<ZaloPayTransaction, Long>, 
        JpaSpecificationExecutor<ZaloPayTransaction> {
    
    /**
     * Tìm transaction theo app_trans_id
     * 
     * @param appTransId Mã giao dịch nội bộ
     * @return Optional transaction
     */
    Optional<ZaloPayTransaction> findByAppTransId(String appTransId);
    
    /**
     * Kiểm tra transaction đã tồn tại chưa
     * 
     * @param appTransId Mã giao dịch nội bộ
     * @return true nếu đã tồn tại
     */
    boolean existsByAppTransId(String appTransId);
    
    /**
     * Tìm transaction theo zp_trans_id
     * 
     * @param zpTransId Mã giao dịch ZaloPay
     * @return Optional transaction
     */
    Optional<ZaloPayTransaction> findByZpTransId(Long zpTransId);
    
    /**
     * Tìm các transaction PENDING được tạo sau thời điểm chỉ định
     * Dùng để query status tự động
     * 
     * @param createdAfter Thời điểm tạo
     * @return List các transaction PENDING
     */
    @Query("SELECT t FROM ZaloPayTransaction t WHERE t.status = 'PENDING' AND t.createdAt > :createdAfter ORDER BY t.createdAt ASC")
    List<ZaloPayTransaction> findPendingTransactionsAfter(@Param("createdAfter") LocalDateTime createdAfter);

    /**
     * Tìm các giao dịch Pending tạo TRƯỚC thời gian threshold (createdAt <= threshold)
     */
    @Query("SELECT t FROM ZaloPayTransaction t WHERE t.status = 'PENDING' AND t.createdAt <= :threshold")
    List<ZaloPayTransaction> findPendingTransactionsBefore(@Param("threshold") LocalDateTime threshold);
}
