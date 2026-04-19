/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command.delete;

public record DeleteProjectCategoryResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
}
