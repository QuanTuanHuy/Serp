/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkItemPlanRepository extends JpaRepository<WorkItemPlanModel, Long> {
    List<WorkItemPlanModel> findAllByTenantIdAndWorkItemIdIn(Long tenantId, List<Long> workItemIds);

    Optional<WorkItemPlanModel> findByTenantIdAndWorkItemId(Long tenantId, Long workItemId);
}
