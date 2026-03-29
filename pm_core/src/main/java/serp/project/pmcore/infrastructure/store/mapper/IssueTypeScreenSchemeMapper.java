/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.infrastructure.store.model.IssueTypeScreenSchemeModel;

@Component
public class IssueTypeScreenSchemeMapper extends BaseMapper {

    public IssueTypeScreenSchemeEntity toEntity(IssueTypeScreenSchemeModel model) {
        if (model == null) { return null; }
        return IssueTypeScreenSchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultScreenSchemeId(model.getDefaultScreenSchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueTypeScreenSchemeModel toModel(IssueTypeScreenSchemeEntity entity) {
        if (entity == null) { return null; }
        return IssueTypeScreenSchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultScreenSchemeId(entity.getDefaultScreenSchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
