/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.DeliveryManifestOrder;
import serp.project.first_mile.enums.DeliveryOrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryManifestOrderRepository extends JpaRepository<DeliveryManifestOrder, Long> {

    Optional<DeliveryManifestOrder> findByManifestIdAndOrderCode(Long manifestId, String orderCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select manifestOrder
            from DeliveryManifestOrder manifestOrder
            where manifestOrder.manifest.id = :manifestId
                and manifestOrder.orderCode = :orderCode
            """)
    Optional<DeliveryManifestOrder> findByManifestIdAndOrderCodeForUpdate(
            @Param("manifestId") Long manifestId,
            @Param("orderCode") String orderCode
    );

    List<DeliveryManifestOrder> findByManifestId(Long manifestId);

    List<DeliveryManifestOrder> findByTenantIdAndOrderCodeAndStatusIn(
            Long tenantId, String orderCode, List<DeliveryOrderStatus> statuses);

    int countByTenantIdAndOrderCode(Long tenantId, String orderCode);
}
