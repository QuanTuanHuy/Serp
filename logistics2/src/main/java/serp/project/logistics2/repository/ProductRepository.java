package serp.project.logistics2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics2.entity.ProductEntity;

public interface ProductRepository
                extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> {

        List<ProductEntity> findByIdInAndTenantId(List<String> ids, Long tenantId);

        Optional<ProductEntity> findByIdAndTenantId(String id, Long tenantId);
}
