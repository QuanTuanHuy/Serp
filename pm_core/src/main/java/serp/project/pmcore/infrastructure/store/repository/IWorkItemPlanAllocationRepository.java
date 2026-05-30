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
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanAllocationModel;

import java.util.List;

@Repository
public interface IWorkItemPlanAllocationRepository extends JpaRepository<WorkItemPlanAllocationModel, Long> {
    List<WorkItemPlanAllocationModel> findAllByTenantIdAndWorkItemPlanId(Long tenantId, Long workItemPlanId);

    List<WorkItemPlanAllocationModel> findAllByTenantIdAndWorkItemPlanIdIn(Long tenantId, List<Long> workItemPlanIds);

    @Modifying
    @Query(value = """
            DELETE FROM work_item_plan_allocations
            WHERE tenant_id = :tenantId
              AND work_item_plan_id = :workItemPlanId
            """, nativeQuery = true)
    int deleteByTenantIdAndWorkItemPlanId(@Param("tenantId") Long tenantId,
                                          @Param("workItemPlanId") Long workItemPlanId);
}
