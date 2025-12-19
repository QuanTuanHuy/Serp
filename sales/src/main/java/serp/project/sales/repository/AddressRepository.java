package serp.project.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.sales.entity.AddressEntity;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity,String> {

    public List<AddressEntity> findByTenantIdAndEntityId(Long tenantId, String entityId);

}
