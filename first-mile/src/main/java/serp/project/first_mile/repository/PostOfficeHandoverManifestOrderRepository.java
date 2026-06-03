/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOfficeHandoverManifestOrder;
import serp.project.first_mile.enums.HandoverManifestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostOfficeHandoverManifestOrderRepository extends JpaRepository<PostOfficeHandoverManifestOrder, Long> {
    List<PostOfficeHandoverManifestOrder> findByManifest_IdAndTenantId(Long manifestId, Long tenantId);

    Optional<PostOfficeHandoverManifestOrder> findByManifest_IdAndOrderCodeIgnoreCaseAndTenantId(
            Long manifestId,
            String orderCode,
            Long tenantId
    );

    List<PostOfficeHandoverManifestOrder> findByManifest_IdAndOrderCodeInAndTenantId(
            Long manifestId,
            Collection<String> orderCodes,
            Long tenantId
    );

    boolean existsByOrderIdAndTenantIdAndManifest_StatusIn(
            Long orderId,
            Long tenantId,
            Collection<HandoverManifestStatus> statuses
    );
}
