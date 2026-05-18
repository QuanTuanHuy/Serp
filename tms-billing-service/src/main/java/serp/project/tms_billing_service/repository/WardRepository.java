package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.Ward;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
}
