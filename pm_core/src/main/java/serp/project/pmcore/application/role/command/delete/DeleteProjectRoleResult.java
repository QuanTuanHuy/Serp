/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command.delete;

public record DeleteProjectRoleResult(
        Long id,
        boolean deleted,
        Long deletedAt,
        Long updatedBy
) {
}
