/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.archive;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.skill.SkillView;

public record ArchiveSkillCommand(
        Long skillId,
        Long tenantId,
        Long userId
) implements ICommand<SkillView> {
}
