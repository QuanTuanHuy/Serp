package serp.project.logistics.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import serp.project.logistics.entity.OrderEntity;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    @Modifying
    @Query("UPDATE OrderEntity o SET o.statusId = :statusId WHERE o.tenantId = :tenantId AND o.id = :orderId")
    public void updateOrderStatus(String orderId, String statusId, Long tenantId);

    @Query("SELECT o.statusId FROM OrderEntity o WHERE o.tenantId = :tenantId AND o.id = :orderId")
    public String getOrderStatus(String orderId, Long tenantId);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.statusId = 'CREATED', " +
            "o.userApprovedId = null, " +
            "o.userCancelledId = null, " +
            "o.cancellationNote = '' " +
            "WHERE o.tenantId = :tenantId AND o.id = :orderId")
    public void resetOrderStatus(String orderId, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.id = :id AND o.tenantId = :tenantId")
    Optional<OrderEntity> findByIdAndTenantIdWithLock(@Param("id") Long id,  @Param("tenantId") Long tenantId);

}
