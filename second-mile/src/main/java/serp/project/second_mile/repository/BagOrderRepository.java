/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.BagOrder;

import java.util.List;
import java.util.Optional;

@Repository
public interface BagOrderRepository extends JpaRepository<BagOrder, Long> {
    List<BagOrder> findByBag_IdAndTenantId(Long bagId, Long tenantId);

    boolean existsByBag_IdAndOrder_IdAndTenantId(Long bagId, Long orderId, Long tenantId);

    boolean existsByOrder_IdAndTenantId(Long orderId, Long tenantId);

    List<BagOrder> findByOrder_IdInAndTenantId(List<Long> orderIds, Long tenantId);

    Optional<BagOrder> findByBag_IdAndOrder_OrderCodeIgnoreCaseAndTenantId(Long bagId, String orderCode, Long tenantId);
}
