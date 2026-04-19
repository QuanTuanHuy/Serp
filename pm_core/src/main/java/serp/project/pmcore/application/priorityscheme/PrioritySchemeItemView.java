/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;

public record PrioritySchemeItemView(
        Long id,
        Long priorityId,
        Integer sequence,
        PrioritySchemePriorityView priority
) {
    public static PrioritySchemeItemView from(PrioritySchemeItemEntity entity,
                                              PrioritySchemePriorityView priority) {
        return new PrioritySchemeItemView(
                entity.getId(),
                entity.getPriorityId(),
                entity.getSequence(),
                priority
        );
    }
}
