package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics.entity.FacilityEntity;

public interface FacilityRepository extends JpaRepository<FacilityEntity, String>, JpaSpecificationExecutor<FacilityEntity> {
}
