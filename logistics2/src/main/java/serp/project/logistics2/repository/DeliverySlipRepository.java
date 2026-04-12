package serp.project.logistics2.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.logistics2.entity.DeliverySlipEntity;

import java.util.Optional;

public interface DeliverySlipRepository extends JpaRepository<DeliverySlipEntity, String> {

        @Query("SELECT s FROM DeliverySlipEntity s " +
                        "WHERE (:status IS NULL OR s.status = :status) " +
                        "AND (:outboundShipmentId IS NULL OR s.outboundShipmentId = :outboundShipmentId)" +
                        "AND (:customerId IS NULL OR s.customerId = :customerId) " +
                        "AND (:facilityId IS NULL OR s.facilityId = :facilityId) " +
                        "AND (:query IS NULL OR s.code LIKE %:query% OR s.customerName LIKE %:query% OR s.customerPhone LIKE %:query%) "
                        +
                        "AND s.tenantId = :tenantId")
        Page<DeliverySlipEntity> search(
                        @Param("status") String status,
                        @Param("outboundShipmentId") String outboundShipmentId,
                        @Param("customerId") String customerId,
                        @Param("facilityId") String facilityId,
                        @Param("query") String query,
                        @Param("tenantId") Long tenantId,
                        Pageable pageable);

        @Query("SELECT COUNT(s) FROM DeliverySlipEntity s WHERE s.outboundShipmentId = :outboundShipmentId AND s.status <> 'DELIVERED'")
        long countPendingSlipByOutboundShipmentId(@Param("outboundShipmentId") String outboundShipmentId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM DeliverySlipEntity s WHERE s.id = :id AND s.tenantId = :tenantId")
        Optional<DeliverySlipEntity> findByIdAndTenantIdWithLock(@Param("id") String id,
                        @Param("tenantId") Long tenantId);

        Optional<DeliverySlipEntity> findByIdAndTenantId(String id, Long tenantId);

        @Modifying
        @Query("UPDATE DeliverySlipEntity s SET s.status = :status WHERE s.id = :id AND s.tenantId = :tenantId")
        void updateStatusByIdAndTenantId(@Param("status") String status, @Param("id") String id,
                        @Param("tenantId") Long tenantId);

        @Modifying
        @Query("UPDATE DeliverySlipEntity s SET s.status = :status WHERE s.id IN :ids AND s.tenantId = :tenantId")
        void updateStatusByIdInAndTenantId(@Param("status") String status, @Param("ids") Iterable<String> ids,
                        @Param("tenantId") Long tenantId);

}
