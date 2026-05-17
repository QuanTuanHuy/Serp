/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.skill.SkillView;

import java.util.List;

public record ListSkillsQuery(Long tenantId) implements IQuery<List<SkillView>> {
}
