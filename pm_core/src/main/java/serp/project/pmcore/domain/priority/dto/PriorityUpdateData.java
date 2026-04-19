/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.dto;

public record PriorityUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        String iconUrl,
        boolean iconUrlProvided,
        String color,
        boolean colorProvided,
        Integer sequence,
        boolean sequenceProvided
) {
}
