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

    boolean existsByBag_IdAndTmsOrderIdAndTenantId(Long bagId, Long tmsOrderId, Long tenantId);

    boolean existsByTmsOrderIdAndTenantId(Long tmsOrderId, Long tenantId);

    List<BagOrder> findByTmsOrderIdInAndTenantId(List<Long> tmsOrderIds, Long tenantId);

    Optional<BagOrder> findByBag_IdAndOrderCodeIgnoreCaseAndTenantId(Long bagId, String orderCode, Long tenantId);
}
