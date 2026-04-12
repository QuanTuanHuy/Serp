package serp.project.logistics2.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import serp.project.logistics2.entity.DeliveryItemEntity;
import serp.project.logistics2.entity.OutboundShipmentItemEntity;

import java.util.List;
import java.util.Optional;

public interface DeliveryItemRepository extends CrudRepository<DeliveryItemEntity, Integer> {

    List<DeliveryItemEntity> findByTenantIdAndDeliverySlipId(Long tenantId, String slipId);

    void deleteByDeliverySlipId(String slipId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM DeliveryItemEntity i WHERE i.id = :id AND i.tenantId = :tenantId")
    Optional<DeliveryItemEntity> findByIdAndTenantIdWithLock(@Param("id") String id,
                                                                     @Param("tenantId") Long tenantId);

}
