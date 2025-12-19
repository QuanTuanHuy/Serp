package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.logistics.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, String>, JpaSpecificationExecutor<CategoryEntity> {



}
