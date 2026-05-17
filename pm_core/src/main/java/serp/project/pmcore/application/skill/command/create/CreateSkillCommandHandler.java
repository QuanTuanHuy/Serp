/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.skill.SkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

@Service
@RequiredArgsConstructor
public class CreateSkillCommandHandler implements ICommandHandler<CreateSkillCommand, SkillView> {
    private final ISkillService skillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillView handle(CreateSkillCommand command) {
        return SkillView.from(skillService.createSkill(
                command.tenantId(),
                command.userId(),
                command.code(),
                command.name(),
                command.description()
        ));
    }
}
