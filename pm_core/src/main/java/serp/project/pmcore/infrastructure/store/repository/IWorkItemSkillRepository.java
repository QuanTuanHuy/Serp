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
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;

import java.util.List;

@Repository
public interface IWorkItemSkillRepository extends JpaRepository<WorkItemSkillModel, Long> {
    List<WorkItemSkillModel> findAllByTenantIdAndWorkItemIdIn(Long tenantId, List<Long> workItemIds);

    List<WorkItemSkillModel> findAllByTenantIdAndProjectIdAndWorkItemId(Long tenantId, Long projectId, Long workItemId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            DELETE FROM WorkItemSkillModel w
            WHERE w.tenantId = :tenantId
              AND w.projectId = :projectId
              AND w.workItemId = :workItemId
            """)
    void deleteAllByTenantIdAndProjectIdAndWorkItemId(
            @Param("tenantId") Long tenantId,
            @Param("projectId") Long projectId,
            @Param("workItemId") Long workItemId);
}
