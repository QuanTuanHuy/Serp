package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics.entity.SupplierEntity;

public interface SupplierRepository extends JpaRepository<SupplierEntity,String>, JpaSpecificationExecutor<SupplierEntity> {
}
