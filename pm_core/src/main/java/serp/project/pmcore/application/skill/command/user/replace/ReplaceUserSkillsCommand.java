/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.user.replace;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.domain.skill.dto.UserSkillDraftData;

import java.util.List;

public record ReplaceUserSkillsCommand(
        Long targetUserId,
        List<UserSkillDraftData> items,
        Long tenantId,
        Long actorUserId
) implements ICommand<List<UserSkillView>> {
}
