/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.user.replace;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplaceUserSkillsCommandHandler
        implements ICommandHandler<ReplaceUserSkillsCommand, List<UserSkillView>> {
    private final ISkillService skillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserSkillView> handle(ReplaceUserSkillsCommand command) {
        return skillService.replaceUserSkills(
                        command.tenantId(),
                        command.targetUserId(),
                        command.actorUserId(),
                        command.items()
                ).stream()
                .map(UserSkillView::from)
                .toList();
    }
}
