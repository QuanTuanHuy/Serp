/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.domain.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.ptm_optimization.core.domain.dto.message.CreateTaskMessage;
import serp.project.ptm_optimization.core.domain.entity.ParentTaskEntity;
import serp.project.ptm_optimization.core.domain.entity.TaskEntity;
import serp.project.ptm_optimization.core.domain.enums.PriorityEnum;

@Component
@RequiredArgsConstructor
public class TaskDtoMapper {

    public TaskEntity toEntity(CreateTaskMessage message, ParentTaskEntity parentTask) {
        return TaskEntity.builder()
                .title(message.getTitle())
                .priority(PriorityEnum.calculateWeightOfPriorities(message.getPriority()))
                .status(message.getStatus())
                .startDate(message.getStartDate())
                .endDate(message.getDeadline())
                .activeStatus(message.getActiveStatus())
                .originalId(message.getTaskId())
                .duration(message.getDuration())
                .parentTaskId(parentTask.getId())
                .build();
    }

    public ParentTaskEntity toParentTaskEntity(CreateTaskMessage message) {
        return ParentTaskEntity.builder()
                .groupTaskId(message.getGroupTaskId())
                .projectId(message.getProjectId())
                .userId(message.getUserId())
                .build();
    }
}
