/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.workitem.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.skill.WorkItemSkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListWorkItemSkillsQueryHandler
        implements IQueryHandler<ListWorkItemSkillsQuery, List<WorkItemSkillView>> {
    private final ISkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public List<WorkItemSkillView> handle(ListWorkItemSkillsQuery query) {
        return skillService.listWorkItemSkills(query.tenantId(), query.projectId(), query.workItemId()).stream()
                .map(WorkItemSkillView::from)
                .toList();
    }
}
