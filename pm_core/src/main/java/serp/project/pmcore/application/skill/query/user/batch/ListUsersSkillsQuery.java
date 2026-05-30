/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.user.batch;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.skill.UserSkillView;

import java.util.List;
import java.util.Map;

public record ListUsersSkillsQuery(
        List<Long> userIds,
        Long tenantId
) implements IQuery<Map<Long, List<UserSkillView>>> {
}
