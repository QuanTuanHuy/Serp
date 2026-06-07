/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.command.delete;

public record DeleteProjectComponentResult(
        Long componentId,
        boolean deleted,
        Long deletedAt,
        Long deletedBy
) {
}
