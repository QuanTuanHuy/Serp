/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueTypeSchemeItemMapper extends BaseMapper {

    public IssueTypeSchemeItemEntity toEntity(IssueTypeSchemeItemModel model) {
        if (model == null) { return null; }
        return IssueTypeSchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .issueTypeId(model.getIssueTypeId())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueTypeSchemeItemModel toModel(IssueTypeSchemeItemEntity entity) {
        if (entity == null) { return null; }
        return IssueTypeSchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .issueTypeId(entity.getIssueTypeId())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<IssueTypeSchemeItemEntity> toEntities(List<IssueTypeSchemeItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<IssueTypeSchemeItemModel> toModels(List<IssueTypeSchemeItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
