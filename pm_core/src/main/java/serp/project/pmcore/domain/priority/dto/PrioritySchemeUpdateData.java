/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.dto;

public record PrioritySchemeUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        Long defaultPriorityId,
        boolean defaultPriorityIdProvided
) {
}
