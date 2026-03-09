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
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeModel;

import java.util.Optional;

@Repository
public interface IWorkflowSchemeRepository extends JpaRepository<WorkflowSchemeModel, Long> {

    Optional<WorkflowSchemeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT s FROM WorkflowSchemeModel s WHERE s.id = :id AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<WorkflowSchemeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("UPDATE WorkflowSchemeModel s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id AND s.tenantId = :tenantId AND s.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
