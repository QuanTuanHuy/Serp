package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
}
