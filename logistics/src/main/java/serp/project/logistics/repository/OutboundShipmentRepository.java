package serp.project.logistics.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.logistics.entity.OutboundShipmentEntity;

import java.util.Optional;

public interface OutboundShipmentRepository extends JpaRepository<OutboundShipmentEntity, String> {

    @Query("SELECT o FROM OutboundShipmentEntity o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:orderId IS NULL OR o.orderId = :orderId)" +
            "AND (:facilityId IS NULL OR o.facilityId = :facilityId) " +
            "AND (:name IS NULL OR o.name LIKE %:name%)" +
            "AND o.tenantId = :tenantId")
    Page<OutboundShipmentEntity> search(
            @Param("status") String status,
            @Param("orderId") String orderId,
            @Param("facilityId") String facilityId,
            @Param("name") String name,
            @Param("tenantId") Long tenantId,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM OutboundShipmentEntity s WHERE s.orderId = :orderId AND s.status = 'CREATED'")
    long countPendingShipmentByOrderId(@Param("orderId") String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OutboundShipmentEntity s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<OutboundShipmentEntity> findByIdAndTenantIdWithLock(@Param("id") String id, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("UPDATE OutboundShipmentEntity s SET s.status = :status WHERE s.id = :id AND s.tenantId = :tenantId")
    void updateStatusByIdAndTenantId(@Param("status") String status, @Param("id") String id, @Param("tenantId") Long tenantId);

}
