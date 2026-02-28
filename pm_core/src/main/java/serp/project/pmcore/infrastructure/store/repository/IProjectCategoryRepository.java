/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.ProjectCategoryModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProjectCategoryRepository extends JpaRepository<ProjectCategoryModel, Long> {

    Optional<ProjectCategoryModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM ProjectCategoryModel c WHERE c.id = :id AND (c.tenantId = :tenantId OR c.tenantId = 0)")
    Optional<ProjectCategoryModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    List<ProjectCategoryModel> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    @Modifying
    @Query("UPDATE ProjectCategoryModel c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id AND c.tenantId = :tenantId AND c.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
