/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;
import serp.project.ptm_optimization.infrastructure.store.model.ParentTaskModel;

@Component
@RequiredArgsConstructor
public class ParentTaskMapper extends BaseMapper {

    public ParentTaskEntity toEntity(ParentTaskModel model) {
        if (model == null) {
            return null;
        }

        return ParentTaskEntity.builder()
                .id(model.getId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .groupTaskId(model.getGroupTaskId())
                .groupTaskName(model.getGroupTaskName())
                .projectId(model.getProjectId())
                .projectName(model.getProjectName())
                .schedulePlanId(model.getSchedulePlanId())
                .schedulePlanName(model.getSchedulePlanName())
                .userId(model.getUserId())
                .build();
    }

    public ParentTaskModel toModel(ParentTaskEntity entity) {
        if (entity == null) {
            return null;
        }

        return ParentTaskModel.builder()
                .id(entity.getId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .groupTaskId(entity.getGroupTaskId())
                .groupTaskName(entity.getGroupTaskName())
                .projectId(entity.getProjectId())
                .projectName(entity.getProjectName())
                .schedulePlanId(entity.getSchedulePlanId())
                .schedulePlanName(entity.getSchedulePlanName())
                .userId(entity.getUserId())
                .build();
    }
}
