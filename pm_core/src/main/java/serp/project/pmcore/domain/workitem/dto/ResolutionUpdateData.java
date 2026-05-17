/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record ResolutionUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        Integer sequence,
        boolean sequenceProvided
) {
}
