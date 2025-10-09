/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.core.domain.entity.TaskEntity;
import serp.project.ptm_optimization.core.domain.enums.ActiveStatusEnum;
import serp.project.ptm_optimization.core.domain.enums.TaskStatusEnum;
import serp.project.ptm_optimization.infrastructure.store.model.TaskModel;

@Component
public class TaskMapper extends BaseMapper {

    public TaskEntity toEntity(TaskModel model) {
        if (model == null) {
            return null;
        }

        return TaskEntity.builder()
                .id(model.getId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .title(model.getTitle())
                .priority(model.getPriority())
                .status(convertStringToEnum(model.getStatus(), TaskStatusEnum.class))
                .startDate(model.getStartDate())
                .endDate(model.getEndDate())
                .activeStatus(convertStringToEnum(model.getActiveStatus(), ActiveStatusEnum.class))
                .originalId(model.getOriginalId())
                .scheduleTaskId(model.getScheduleTaskId())
                .taskOrder(model.getTaskOrder())
                .effort(model.getEffort())
                .enjoyability(model.getEnjoyability())
                .duration(model.getDuration())
                .weight(model.getWeight())
                .stopTime(model.getStopTime())
                .taskBatch(model.getTaskBatch())
                .parentTaskId(model.getParentTaskId())
                .build();
    }

    public TaskModel toModel(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        return TaskModel.builder()
                .id(entity.getId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .title(entity.getTitle())
                .priority(entity.getPriority())
                .status(convertEnumToDisplayString(entity.getStatus(), "getValue"))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .activeStatus(convertEnumToDisplayString(entity.getActiveStatus(), "getValue"))
                .originalId(entity.getOriginalId())
                .scheduleTaskId(entity.getScheduleTaskId())
                .taskOrder(entity.getTaskOrder())
                .effort(entity.getEffort())
                .enjoyability(entity.getEnjoyability())
                .duration(entity.getDuration())
                .weight(entity.getWeight())
                .stopTime(entity.getStopTime())
                .taskBatch(entity.getTaskBatch())
                .parentTaskId(entity.getParentTaskId())
                .build();
    }

    public List<TaskEntity> toEntityList(List<TaskModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .toList();
    }
}
