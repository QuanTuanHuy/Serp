/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.infrastructure.store.model.UserSkillModel;

import java.util.Collections;
import java.util.List;

@Component
public class UserSkillMapper extends BaseMapper {
    public UserSkillModel toModel(UserSkillEntity entity) {
        if (entity == null) { return null; }
        return UserSkillModel.builder()
                .id(entity.getId()).tenantId(entity.getTenantId()).userId(entity.getUserId())
                .skillId(entity.getSkillId()).proficiency(entity.getProficiency()).confidence(entity.getConfidence())
                .source(entity.getSource()).verifiedAt(longToLocalDateTime(entity.getVerifiedAt()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt())).createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt())).updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt())).build();
    }

    public UserSkillEntity toEntity(UserSkillModel model) {
        if (model == null) { return null; }
        return UserSkillEntity.builder()
                .id(model.getId()).tenantId(model.getTenantId()).userId(model.getUserId())
                .skillId(model.getSkillId()).proficiency(model.getProficiency()).confidence(model.getConfidence())
                .source(model.getSource()).verifiedAt(localDateTimeToLong(model.getVerifiedAt()))
                .createdAt(localDateTimeToLong(model.getCreatedAt())).createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt())).updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt())).build();
    }

    public List<UserSkillEntity> toEntities(List<UserSkillModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }
}
