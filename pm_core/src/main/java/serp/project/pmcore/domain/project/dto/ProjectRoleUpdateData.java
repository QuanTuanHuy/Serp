/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

public record ProjectRoleUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided
) {
}
