/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.archive;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.skill.SkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

@Service
@RequiredArgsConstructor
public class ArchiveSkillCommandHandler implements ICommandHandler<ArchiveSkillCommand, SkillView> {
    private final ISkillService skillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillView handle(ArchiveSkillCommand command) {
        return SkillView.from(skillService.archiveSkill(
                command.tenantId(),
                command.userId(),
                command.skillId()
        ));
    }
}
