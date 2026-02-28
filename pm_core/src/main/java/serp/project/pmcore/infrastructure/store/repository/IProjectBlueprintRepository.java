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
import serp.project.pmcore.infrastructure.store.model.ProjectBlueprintModel;

import java.util.Optional;

@Repository
public interface IProjectBlueprintRepository extends JpaRepository<ProjectBlueprintModel, Long> {

    Optional<ProjectBlueprintModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT b FROM ProjectBlueprintModel b WHERE b.id = :id AND (b.tenantId = :tenantId OR b.tenantId = 0)")
    Optional<ProjectBlueprintModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("UPDATE ProjectBlueprintModel b SET b.deletedAt = CURRENT_TIMESTAMP WHERE b.id = :id AND b.tenantId = :tenantId AND b.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
