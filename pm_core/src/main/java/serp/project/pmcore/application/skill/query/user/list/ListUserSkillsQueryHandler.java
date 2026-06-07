/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.user.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUserSkillsQueryHandler implements IQueryHandler<ListUserSkillsQuery, List<UserSkillView>> {
    private final ISkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillView> handle(ListUserSkillsQuery query) {
        return skillService.listUserSkills(query.tenantId(), query.targetUserId()).stream()
                .map(UserSkillView::from)
                .toList();
    }
}
