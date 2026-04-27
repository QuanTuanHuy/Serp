/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record StatusCategoryUpdateData(
        String name,
        boolean nameProvided,
        String key,
        boolean keyProvided,
        String color,
        boolean colorProvided
) {
}
