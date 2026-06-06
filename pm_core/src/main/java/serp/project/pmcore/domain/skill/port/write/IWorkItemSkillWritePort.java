/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.write;

import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;

import java.util.List;

public interface IWorkItemSkillWritePort {
    void deleteActive(Long tenantId, Long projectId, Long workItemId);

    List<WorkItemSkillEntity> saveAll(List<WorkItemSkillEntity> skills);
}
