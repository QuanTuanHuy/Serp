/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.IssueTypeScreenSchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueTypeScreenSchemeItemMapper extends BaseMapper {

    public IssueTypeScreenSchemeItemEntity toEntity(IssueTypeScreenSchemeItemModel model) {
        if (model == null) { return null; }
        return IssueTypeScreenSchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .issueTypeId(model.getIssueTypeId())
                .screenSchemeId(model.getScreenSchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueTypeScreenSchemeItemModel toModel(IssueTypeScreenSchemeItemEntity entity) {
        if (entity == null) { return null; }
        return IssueTypeScreenSchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .issueTypeId(entity.getIssueTypeId())
                .screenSchemeId(entity.getScreenSchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<IssueTypeScreenSchemeItemEntity> toEntities(List<IssueTypeScreenSchemeItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<IssueTypeScreenSchemeItemModel> toModels(List<IssueTypeScreenSchemeItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
