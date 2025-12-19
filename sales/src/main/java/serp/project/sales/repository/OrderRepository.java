package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import serp.project.logistics.entity.OrderEntity;

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

}
