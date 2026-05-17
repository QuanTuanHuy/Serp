/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.skill.SkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSkillsQueryHandler implements IQueryHandler<ListSkillsQuery, List<SkillView>> {
    private final ISkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public List<SkillView> handle(ListSkillsQuery query) {
        return skillService.listSkills(query.tenantId()).stream()
                .map(SkillView::from)
                .toList();
    }
}
