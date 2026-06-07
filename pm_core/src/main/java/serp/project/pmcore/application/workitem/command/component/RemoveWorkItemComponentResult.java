/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.component;

public record RemoveWorkItemComponentResult(
        Long workItemId,
        Long componentId,
        boolean removed,
        Long removedAt
) {
}
