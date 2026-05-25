/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;

import java.util.Set;

public record GetProjectSummaryQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        ProjectSummaryCriteria criteria
) implements IQuery<ProjectSummaryView> {
}
