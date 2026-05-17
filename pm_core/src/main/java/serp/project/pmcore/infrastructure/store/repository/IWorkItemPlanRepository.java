/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkItemPlanRepository extends JpaRepository<WorkItemPlanModel, Long> {
    List<WorkItemPlanModel> findAllByTenantIdAndWorkItemIdIn(Long tenantId, List<Long> workItemIds);

    Optional<WorkItemPlanModel> findByTenantIdAndWorkItemId(Long tenantId, Long workItemId);

    @Query("""
            SELECT p FROM WorkItemPlanModel p
            JOIN WorkItemModel w ON w.id = p.workItemId AND w.tenantId = p.tenantId
            WHERE p.tenantId = :tenantId
              AND w.assigneeId IN :assigneeIds
              AND w.resolutionId IS NULL
              AND p.plannedStart < :planningEnd
              AND p.plannedEnd > :planningStart
              AND p.workItemId NOT IN :excludedWorkItemIds
            """)
    List<WorkItemPlanModel> findActiveWorkloadPlans(@Param("tenantId") Long tenantId,
                                                     @Param("assigneeIds") List<Long> assigneeIds,
                                                     @Param("planningStart") java.time.LocalDateTime planningStart,
                                                     @Param("planningEnd") java.time.LocalDateTime planningEnd,
                                                     @Param("excludedWorkItemIds") List<Long> excludedWorkItemIds);
}
