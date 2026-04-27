/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

import java.util.List;
import java.util.Map;

public record PrioritySchemeDetailView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultPriorityId,
        boolean isSystem,
        boolean readOnly,
        List<PrioritySchemeItemView> items,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static PrioritySchemeDetailView from(PrioritySchemeEntity entity,
                                                Map<Long, PrioritySchemePriorityView> prioritiesById) {
        List<PrioritySchemeItemView> itemViews = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                .map(item -> PrioritySchemeItemView.from(item, prioritiesById.get(item.getPriorityId())))
                .toList();

        return new PrioritySchemeDetailView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultPriorityId(),
                entity.isSystem(),
                entity.isSystem(),
                itemViews,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
