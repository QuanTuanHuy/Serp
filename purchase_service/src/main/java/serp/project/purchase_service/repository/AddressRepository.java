package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.AddressEntity;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity,String> {

    public List<AddressEntity> findByEntityIdAndTenantId(String entityId, Long tenantId);

}
