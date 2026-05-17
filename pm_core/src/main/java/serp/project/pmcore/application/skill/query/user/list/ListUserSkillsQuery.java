/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.user.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.skill.UserSkillView;

import java.util.List;

public record ListUserSkillsQuery(
        Long targetUserId,
        Long tenantId
) implements IQuery<List<UserSkillView>> {
}
