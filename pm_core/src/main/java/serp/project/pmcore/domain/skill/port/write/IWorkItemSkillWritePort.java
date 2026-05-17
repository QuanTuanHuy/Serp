/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.write;

import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;

import java.util.List;

public interface IWorkItemSkillWritePort {
    void softDeleteActive(Long tenantId, Long projectId, Long workItemId, Long userId, Long now);

    List<WorkItemSkillEntity> saveAll(List<WorkItemSkillEntity> skills);
}
