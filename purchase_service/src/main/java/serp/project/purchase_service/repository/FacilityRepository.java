package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.purchase_service.entity.FacilityEntity;

public interface FacilityRepository extends JpaRepository<FacilityEntity, String>, JpaSpecificationExecutor<FacilityEntity> {
}
