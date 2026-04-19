/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;

import java.util.List;
import java.util.Map;

public record IssueTypeSchemeDetailView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Long defaultIssueTypeId,
        boolean isSystem,
        boolean readOnly,
        List<IssueTypeSchemeItemView> items,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static IssueTypeSchemeDetailView from(IssueTypeSchemeEntity entity,
                                                 Map<Long, IssueTypeSchemeIssueTypeView> issueTypesById) {
        List<IssueTypeSchemeItemView> itemViews = entity.getItems() == null
                ? List.of()
                : entity.getItems().stream()
                        .map(item -> IssueTypeSchemeItemView.from(item, issueTypesById.get(item.getIssueTypeId())))
                        .toList();

        return new IssueTypeSchemeDetailView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDefaultIssueTypeId(),
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
