/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.read;

import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;

import java.util.List;

public interface IWorkItemSkillReadPort {
    List<WorkItemSkillEntity> listActive(Long tenantId, Long projectId, Long workItemId);

    List<WorkItemSkillEntity> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds);
}
