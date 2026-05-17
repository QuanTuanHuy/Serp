/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.skill.SkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

@Service
@RequiredArgsConstructor
public class UpdateSkillCommandHandler implements ICommandHandler<UpdateSkillCommand, SkillView> {
    private final ISkillService skillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillView handle(UpdateSkillCommand command) {
        return SkillView.from(skillService.updateSkill(
                command.tenantId(),
                command.userId(),
                command.skillId(),
                command.code(),
                command.name(),
                command.description()
        ));
    }
}
