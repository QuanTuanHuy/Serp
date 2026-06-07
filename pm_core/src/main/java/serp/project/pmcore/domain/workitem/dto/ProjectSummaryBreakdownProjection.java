/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record ProjectSummaryBreakdownProjection(
        Long id,
        String key,
        String name,
        String iconUrl,
        String color,
        Integer sequence,
        String categoryKey,
        String categoryName,
        long count
) {
}
