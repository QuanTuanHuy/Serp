/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.dependencies;

public record WorkItemDependencyNodeView(
        Long id,
        Long projectId,
        String key,
        String summary,
        Long statusId,
        String statusName,
        Long issueTypeId,
        String issueTypeName,
        Long priorityId,
        String priorityName,
        Long assigneeId,
        String assigneeName,
        Long dueDate,
        Long plannedStart,
        Long plannedEnd,
        boolean outsideFilter,
        int blockedByCount,
        int blocksCount,
        boolean hasCycle
) {
}
