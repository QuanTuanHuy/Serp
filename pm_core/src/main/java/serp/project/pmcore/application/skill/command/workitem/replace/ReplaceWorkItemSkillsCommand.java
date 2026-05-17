/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.workitem.replace;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.skill.WorkItemSkillView;
import serp.project.pmcore.domain.skill.dto.WorkItemSkillDraftData;

import java.util.List;

public record ReplaceWorkItemSkillsCommand(
        Long projectId,
        Long workItemId,
        List<WorkItemSkillDraftData> items,
        Long tenantId,
        Long userId
) implements ICommand<List<WorkItemSkillView>> {
}
