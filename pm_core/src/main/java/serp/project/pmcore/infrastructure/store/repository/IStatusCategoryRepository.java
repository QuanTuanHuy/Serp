/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.StatusCategoryModel;

import java.util.Optional;

@Repository
public interface IStatusCategoryRepository extends JpaRepository<StatusCategoryModel, Long> {

    Optional<StatusCategoryModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT c FROM StatusCategoryModel c WHERE c.id = :id AND (c.tenantId = :tenantId OR c.tenantId = 0)")
    Optional<StatusCategoryModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    Optional<StatusCategoryModel> findFirstByTenantIdAndKeyOrderByIdAsc(Long tenantId, String key);
}
