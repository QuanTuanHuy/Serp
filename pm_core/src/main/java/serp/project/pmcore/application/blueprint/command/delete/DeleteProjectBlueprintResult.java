/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.command.delete;

public record DeleteProjectBlueprintResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
}
