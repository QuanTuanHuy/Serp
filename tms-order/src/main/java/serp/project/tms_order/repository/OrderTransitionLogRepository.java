/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.OrderTransitionLog;

import java.util.Optional;

@Repository
public interface OrderTransitionLogRepository extends JpaRepository<OrderTransitionLog, Long> {
    Optional<OrderTransitionLog> findByTenantIdAndIdempotencyKey(Long tenantId, String idempotencyKey);
}
