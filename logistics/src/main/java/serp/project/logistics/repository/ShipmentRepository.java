package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics.entity.ShipmentEntity;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, String>, JpaSpecificationExecutor<ShipmentEntity> {

    public List<ShipmentEntity> findByTenantIdAndOrderId(Long tenantId, String orderId);

    public List<ShipmentEntity> findByTenantIdAndOrderIdAndStatusId(Long tenantId, String orderId, String statusId);


}
