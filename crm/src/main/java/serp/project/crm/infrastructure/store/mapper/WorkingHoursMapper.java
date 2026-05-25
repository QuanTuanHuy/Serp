/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.WorkingHoursEntity;
import serp.project.crm.infrastructure.store.model.WorkingHoursModel;

import java.time.DayOfWeek;
import java.util.List;

@Component
public class WorkingHoursMapper extends BaseMapper {

    public WorkingHoursEntity toEntity(WorkingHoursModel model) {
        if (model == null) {
            return null;
        }

        return WorkingHoursEntity.builder()
                .id(model.getId())
                .teamMemberId(model.getTeamMemberId())
                .dayOfWeek(stringToEnum(model.getDayOfWeek(), DayOfWeek.class))
                .workingDay(model.getWorkingDay())
                .startMinute(model.getStartMinute())
                .endMinute(model.getEndMinute())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public WorkingHoursModel toModel(WorkingHoursEntity entity) {
        if (entity == null) {
            return null;
        }

        return WorkingHoursModel.builder()
                .id(entity.getId())
                .teamMemberId(entity.getTeamMemberId())
                .dayOfWeek(enumToString(entity.getDayOfWeek()))
                .workingDay(entity.getWorkingDay())
                .startMinute(entity.getStartMinute())
                .endMinute(entity.getEndMinute())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<WorkingHoursEntity> toEntityList(List<WorkingHoursModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkingHoursModel> toModelList(List<WorkingHoursEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toModel).toList();
    }
}
