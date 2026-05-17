/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkItemSkillMapper extends BaseMapper {
    public WorkItemSkillModel toModel(WorkItemSkillEntity entity) {
        if (entity == null) { return null; }
        return WorkItemSkillModel.builder()
                .id(entity.getId()).tenantId(entity.getTenantId()).projectId(entity.getProjectId())
                .workItemId(entity.getWorkItemId()).skillId(entity.getSkillId())
                .requirementType(entity.getRequirementType()).minProficiency(entity.getMinProficiency())
                .weight(entity.getWeight()).source(entity.getSource())
                .createdAt(longToLocalDateTime(entity.getCreatedAt())).createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt())).updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt())).build();
    }

    public WorkItemSkillEntity toEntity(WorkItemSkillModel model) {
        if (model == null) { return null; }
        return WorkItemSkillEntity.builder()
                .id(model.getId()).tenantId(model.getTenantId()).projectId(model.getProjectId())
                .workItemId(model.getWorkItemId()).skillId(model.getSkillId())
                .requirementType(model.getRequirementType()).minProficiency(model.getMinProficiency())
                .weight(model.getWeight()).source(model.getSource())
                .createdAt(localDateTimeToLong(model.getCreatedAt())).createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt())).updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt())).build();
    }

    public List<WorkItemSkillEntity> toEntities(List<WorkItemSkillModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }
}
