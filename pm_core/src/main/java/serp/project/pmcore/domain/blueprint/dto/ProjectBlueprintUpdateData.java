/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.dto;

public record ProjectBlueprintUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        String avatarUrl,
        boolean avatarUrlProvided
) {
}
