package serp.project.logistics.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.logistics.entity.OutboundShipmentItemEntity;

import java.util.List;
import java.util.Optional;

public interface OutboundShipmentItemRepository extends JpaRepository<OutboundShipmentItemEntity, String> {

    List<OutboundShipmentItemEntity> findByTenantIdAndOutboundShipmentId(Long tenantId, String shipmentId);

    void deleteByOutboundShipmentId(String shipmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM OutboundShipmentItemEntity i WHERE i.id = :id AND i.tenantId = :tenantId")
    Optional<OutboundShipmentItemEntity> findByIdAndTenantIdWithLock(@Param("id") String id, @Param("tenantId") Long tenantId);

}
