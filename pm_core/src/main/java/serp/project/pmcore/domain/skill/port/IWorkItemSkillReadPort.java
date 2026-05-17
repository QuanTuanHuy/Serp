/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port;

import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;

import java.util.List;

public interface IWorkItemSkillReadPort {
    List<WorkItemSkillEntity> listActiveByWorkItemIds(Long tenantId, List<Long> workItemIds);
}
