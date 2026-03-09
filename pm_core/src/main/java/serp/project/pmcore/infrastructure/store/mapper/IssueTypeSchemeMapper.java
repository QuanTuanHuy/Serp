/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueTypeSchemeMapper extends BaseMapper {

    public IssueTypeSchemeEntity toEntity(IssueTypeSchemeModel model) {
        if (model == null) { return null; }
        return IssueTypeSchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultIssueTypeId(model.getDefaultIssueTypeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueTypeSchemeModel toModel(IssueTypeSchemeEntity entity) {
        if (entity == null) { return null; }
        return IssueTypeSchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultIssueTypeId(entity.getDefaultIssueTypeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<IssueTypeSchemeEntity> toEntities(List<IssueTypeSchemeModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
