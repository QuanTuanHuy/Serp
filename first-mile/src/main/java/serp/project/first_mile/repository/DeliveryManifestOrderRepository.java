/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.enums.DeliveryOrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryManifestOrderRepository extends JpaRepository<DeliveryManifestOrder, Long> {

    Optional<DeliveryManifestOrder> findByManifestIdAndOrderCode(Long manifestId, String orderCode);

    List<DeliveryManifestOrder> findByManifestId(Long manifestId);

    List<DeliveryManifestOrder> findByTenantIdAndOrderCodeAndStatusIn(
            Long tenantId, String orderCode, List<DeliveryOrderStatus> statuses);

    int countByTenantIdAndOrderCode(Long tenantId, String orderCode);
}
