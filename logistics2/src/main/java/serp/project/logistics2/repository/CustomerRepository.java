package serp.project.logistics2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics2.entity.CustomerEntity;

public interface CustomerRepository
                extends JpaRepository<CustomerEntity, String>, JpaSpecificationExecutor<CustomerEntity> {

        Optional<CustomerEntity> findByIdAndTenantId(String id, Long tenantId);

}
