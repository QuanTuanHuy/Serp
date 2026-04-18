/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record StatusUpdateData(
        String statusKey,
        boolean statusKeyProvided,
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        String iconUrl,
        boolean iconUrlProvided,
        Long statusCategoryId,
        boolean statusCategoryIdProvided
) {
}
