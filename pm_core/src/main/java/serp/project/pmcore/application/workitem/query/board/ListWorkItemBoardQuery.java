/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardCriteria;

import java.util.Set;

public record ListWorkItemBoardQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemBoardCriteria criteria
) implements IQuery<WorkItemBoardView> {
}
