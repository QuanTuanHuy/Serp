/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

public record ProjectComponentUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        Long leadUserId,
        boolean leadUserIdProvided,
        String assigneeType,
        boolean assigneeTypeProvided
) {
}
