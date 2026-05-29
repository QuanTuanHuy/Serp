/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HandoverManifestOrder;

import java.util.List;

@Repository
public interface HandoverManifestOrderRepository extends JpaRepository<HandoverManifestOrder, Long> {
    List<HandoverManifestOrder> findByManifest_IdAndTenantId(Long manifestId, Long tenantId);

    List<HandoverManifestOrder> findByManifest_IdAndOrder_OrderCodeInAndTenantId(
            Long manifestId,
            List<String> orderCodes,
            Long tenantId
    );
}
