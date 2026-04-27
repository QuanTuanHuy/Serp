package serp.project.logistics2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics2.entity.AddressEntity;

import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, String> {

    public List<AddressEntity> findByTenantIdAndEntityId(Long tenantId, String entityId);

}
