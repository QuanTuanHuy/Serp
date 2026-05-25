/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.create;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.skill.SkillView;

public record CreateSkillCommand(
        String code,
        String name,
        String description,
        Long tenantId,
        Long userId
) implements ICommand<SkillView> {
}
