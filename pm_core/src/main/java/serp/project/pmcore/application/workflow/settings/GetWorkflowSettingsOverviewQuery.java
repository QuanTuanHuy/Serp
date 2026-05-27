/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.settings;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetWorkflowSettingsOverviewQuery(
        Long tenantId
) implements IQuery<WorkflowSettingsOverviewView> {
}
