/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.model;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public record ResolvedWorkItemCreateConfiguration(IssueTypeEntity issueType,
                                                  WorkflowStepEntity initialStep,
                                                  Long priorityId) {
}
