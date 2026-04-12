package serp.project.logistics2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics2.entity.SupplierEntity;

public interface SupplierRepository
        extends JpaRepository<SupplierEntity, String>, JpaSpecificationExecutor<SupplierEntity> {
}
