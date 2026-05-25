/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill.query.workitem.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.skill.WorkItemSkillView;

import java.util.List;

public record ListWorkItemSkillsQuery(
        Long projectId,
        Long workItemId,
        Long tenantId
) implements IQuery<List<WorkItemSkillView>> {
}
