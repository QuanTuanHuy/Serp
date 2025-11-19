package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.purchase_service.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity,String>, JpaSpecificationExecutor<ProductEntity> {
}
