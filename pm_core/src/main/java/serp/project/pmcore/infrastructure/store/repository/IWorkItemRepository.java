/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkItemRepository extends JpaRepository<WorkItemModel, Long> {
    Optional<WorkItemModel> findByIdAndTenantId(Long id, Long tenantId);

    List<WorkItemModel> findAllByTenantIdAndProjectId(Long tenantId, Long projectId);

    List<WorkItemModel> findAllByTenantIdAndIssueTypeId(Long tenantId, Long issueId);

    @Modifying
    @Query("UPDATE WorkItemModel w SET w.deletedAt = CURRENT_TIMESTAMP WHERE w.id = :id AND w.tenantId = :tenantId AND w.deletedAt IS NULL")
    void deleteByIdAndTenantId(Long id, Long tenantId);
}
