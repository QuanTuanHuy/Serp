/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_order.domain.OrderHistory;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    List<OrderHistory> findByOrderIdAndTenantIdOrderByEventTimeDescIdDesc(Long orderId, Long tenantId);
}
