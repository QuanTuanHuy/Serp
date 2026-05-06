package serp.project.payment_service.repository;

import serp.project.payment_service.entity.ZaloPayRefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ZaloPayRefundTransaction
 */
@Repository
public interface ZaloPayRefundTransactionRepository extends JpaRepository<ZaloPayRefundTransaction, Long> {
    
    /**
     * Tìm refund transaction theo m_refund_id
     */
    Optional<ZaloPayRefundTransaction> findBymRefundId(String mRefundId);
    
    /**
     * Tìm refund transaction theo zp_trans_id
     */
    Optional<ZaloPayRefundTransaction> findByZpTransId(String zpTransId);
    
    /**
     * Tìm tất cả refund transactions theo status
     */
    List<ZaloPayRefundTransaction> findByStatus(ZaloPayRefundTransaction.RefundStatus status);
}
