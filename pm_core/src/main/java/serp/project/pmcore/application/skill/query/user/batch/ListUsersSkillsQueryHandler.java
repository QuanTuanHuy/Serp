/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.user.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.skill.UserSkillView;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.service.ISkillService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListUsersSkillsQueryHandler implements IQueryHandler<ListUsersSkillsQuery, Map<Long, List<UserSkillView>>> {
    private final ISkillService skillService;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<UserSkillView>> handle(ListUsersSkillsQuery query) {
        List<Long> userIds = query.userIds() == null
                ? List.of()
                : query.userIds().stream().distinct().toList();
        Map<Long, List<UserSkillView>> result = new LinkedHashMap<>();
        userIds.forEach(userId -> result.put(userId, List.of()));
        if (userIds.isEmpty()) {
            return result;
        }

        skillService.listUsersSkills(query.tenantId(), userIds).stream()
                .collect(Collectors.groupingBy(
                        UserSkillEntity::getUserId,
                        LinkedHashMap::new,
                        Collectors.mapping(UserSkillView::from, Collectors.toList())
                ))
                .forEach(result::put);
        return result;
    }
}
