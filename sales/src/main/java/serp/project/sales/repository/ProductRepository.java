package serp.project.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.sales.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity,String>, JpaSpecificationExecutor<ProductEntity> {
}
