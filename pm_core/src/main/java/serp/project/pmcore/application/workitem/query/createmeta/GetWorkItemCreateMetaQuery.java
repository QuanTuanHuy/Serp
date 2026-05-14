/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.createmeta;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record GetWorkItemCreateMetaQuery(
        Long tenantId,
        Long userId,
        Long projectId,
        Long issueTypeId,
        Set<String> groupKeys
) implements IQuery<WorkItemCreateMetaView> {
}
