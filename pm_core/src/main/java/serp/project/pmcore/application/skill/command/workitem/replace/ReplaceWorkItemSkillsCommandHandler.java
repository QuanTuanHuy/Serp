/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.command.workitem.replace;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.skill.WorkItemSkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplaceWorkItemSkillsCommandHandler
        implements ICommandHandler<ReplaceWorkItemSkillsCommand, List<WorkItemSkillView>> {
    private final ISkillService skillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkItemSkillView> handle(ReplaceWorkItemSkillsCommand command) {
        return skillService.replaceWorkItemSkills(
                        command.tenantId(),
                        command.userId(),
                        command.projectId(),
                        command.workItemId(),
                        command.items()
                ).stream()
                .map(WorkItemSkillView::from)
                .toList();
    }
}
