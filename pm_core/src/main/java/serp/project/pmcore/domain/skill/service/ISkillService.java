/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.service;

import serp.project.pmcore.domain.skill.dto.UserSkillDraftData;
import serp.project.pmcore.domain.skill.dto.WorkItemSkillDraftData;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;

import java.util.List;

public interface ISkillService {
    List<SkillEntity> listSkills(Long tenantId);

    SkillEntity createSkill(Long tenantId, Long userId, String code, String name, String description);

    SkillEntity updateSkill(Long tenantId, Long userId, Long skillId, String code, String name, String description);

    SkillEntity archiveSkill(Long tenantId, Long userId, Long skillId);

    List<WorkItemSkillEntity> listWorkItemSkills(Long tenantId, Long projectId, Long workItemId);

    List<WorkItemSkillEntity> replaceWorkItemSkills(
            Long tenantId,
            Long userId,
            Long projectId,
            Long workItemId,
            List<WorkItemSkillDraftData> drafts
    );

    List<UserSkillEntity> listUserSkills(Long tenantId, Long userId);

    List<UserSkillEntity> listUsersSkills(Long tenantId, List<Long> userIds);

    List<UserSkillEntity> replaceUserSkills(
            Long tenantId,
            Long targetUserId,
            Long actorUserId,
            List<UserSkillDraftData> drafts
    );
}
