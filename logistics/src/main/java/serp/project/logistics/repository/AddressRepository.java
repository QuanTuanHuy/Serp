package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics.entity.AddressEntity;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity,String> {

    public List<AddressEntity> findByTenantIdAndEntityId(Long tenantId, String entityId);

}
