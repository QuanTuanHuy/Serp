/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.infrastructure.store.model.SkillModel;

import java.util.Collections;
import java.util.List;

@Component
public class SkillMapper extends BaseMapper {
    public SkillModel toModel(SkillEntity entity) {
        if (entity == null) { return null; }
        return SkillModel.builder()
                .id(entity.getId()).tenantId(entity.getTenantId()).code(entity.getCode()).name(entity.getName())
                .description(entity.getDescription()).active(entity.getActive())
                .createdAt(longToLocalDateTime(entity.getCreatedAt())).createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt())).updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt())).build();
    }

    public SkillEntity toEntity(SkillModel model) {
        if (model == null) { return null; }
        return SkillEntity.builder()
                .id(model.getId()).tenantId(model.getTenantId()).code(model.getCode()).name(model.getName())
                .description(model.getDescription()).active(model.getActive())
                .createdAt(localDateTimeToLong(model.getCreatedAt())).createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt())).updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt())).build();
    }

    public List<SkillEntity> toEntities(List<SkillModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }
}
