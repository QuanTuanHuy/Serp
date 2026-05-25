/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;

public record OptimizationWorkItem(
        WorkItemEntity workItem,
        WorkItemPlanEntity activePlan,
        OptimizationDuration duration,
        OptimizationPriorityScore priorityScore,
        List<OptimizationCandidateAssignee> candidateAssignees,
        boolean done,
        boolean criticalPath
) {
}
