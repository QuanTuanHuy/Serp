/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.workitem.create;

import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;

public record ResolvedWorkItemCreateConfiguration(IssueTypeEntity issueType,
                                                  WorkflowStepEntity initialStep,
                                                  Long priorityId) {
}
