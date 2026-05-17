/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;

import java.util.List;

@Repository
public interface IWorkItemSkillRepository extends JpaRepository<WorkItemSkillModel, Long> {
    List<WorkItemSkillModel> findAllByTenantIdAndWorkItemIdIn(Long tenantId, List<Long> workItemIds);
}
