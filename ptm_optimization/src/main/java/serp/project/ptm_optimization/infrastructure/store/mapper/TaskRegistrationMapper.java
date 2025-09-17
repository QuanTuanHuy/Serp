/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.ptm_optimization.core.domain.dto.request.CreateTaskRegistrationDto;
import serp.project.ptm_optimization.core.domain.entity.TaskRegistrationEntity;
import serp.project.ptm_optimization.infrastructure.store.model.TaskRegistrationModel;

@Component
public class TaskRegistrationMapper extends BaseMapper {

    public TaskRegistrationEntity toEntity(TaskRegistrationModel model) {
        if (model == null) {
            return null;
        }

        return TaskRegistrationEntity.builder()
                .id(model.getId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .userId(model.getUserId())
                .name(model.getName())
                .maxWorkTime(model.getMaxWorkTime())
                .constant1(model.getConstant1())
                .constant2(model.getConstant2())
                .constant3(model.getConstant3())
                .sleepDuration(model.getSleepDuration())
                .startSleepTime(model.getStartSleepTime())
                .endSleepTime(model.getEndSleepTime())
                .relaxTime(model.getRelaxTime())
                .travelTime(model.getTravelTime())
                .eatTime(model.getEatTime())
                .workTime(model.getWorkTime())
                .status(model.getStatus())
                .build();
    }

    public TaskRegistrationModel toModel(TaskRegistrationEntity entity) {
        if (entity == null) {
            return null;
        }

        return TaskRegistrationModel.builder()
                .id(entity.getId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .userId(entity.getUserId())
                .name(entity.getName())
                .maxWorkTime(entity.getMaxWorkTime())
                .constant1(entity.getConstant1())
                .constant2(entity.getConstant2())
                .constant3(entity.getConstant3())
                .sleepDuration(entity.getSleepDuration())
                .startSleepTime(entity.getStartSleepTime())
                .endSleepTime(entity.getEndSleepTime())
                .relaxTime(entity.getRelaxTime())
                .travelTime(entity.getTravelTime())
                .eatTime(entity.getEatTime())
                .workTime(entity.getWorkTime())
                .status(entity.getStatus())
                .build();
    }

    public TaskRegistrationEntity toEntity(Long userId, CreateTaskRegistrationDto req) {
        if (req == null) {
            return null;
        }

        return TaskRegistrationEntity.builder()
                .userId(userId)
                .sleepDuration(req.getSleepDuration())
                .startSleepTime(req.getStartSleepTime())
                .endSleepTime(req.getEndSleepTime())
                .relaxTime(req.getRelaxTime())
                .travelTime(req.getTravelTime())
                .eatTime(req.getEatTime())
                .workTime(req.getWorkTime())
                .build();
    }
}
