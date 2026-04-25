package serp.project.logistics2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics2.entity.FacilityEntity;

public interface FacilityRepository
                extends JpaRepository<FacilityEntity, String>, JpaSpecificationExecutor<FacilityEntity> {

        Optional<FacilityEntity> findByIdAndTenantId(String id, Long tenantId);
}
