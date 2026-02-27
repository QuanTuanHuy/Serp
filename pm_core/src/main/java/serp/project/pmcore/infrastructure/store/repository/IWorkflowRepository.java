/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowModel;

import java.util.Optional;

@Repository
public interface IWorkflowRepository extends JpaRepository<WorkflowModel, Long> {

    Optional<WorkflowModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT w FROM WorkflowModel w WHERE w.id = :id AND (w.tenantId = :tenantId OR w.tenantId = 0)")
    Optional<WorkflowModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
