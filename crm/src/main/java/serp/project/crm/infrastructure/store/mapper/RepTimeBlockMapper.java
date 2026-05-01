/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.RepTimeBlockEntity;
import serp.project.crm.core.domain.enums.RepTimeBlockType;
import serp.project.crm.infrastructure.store.model.RepTimeBlockModel;

import java.util.List;

@Component
public class RepTimeBlockMapper extends BaseMapper {

    public RepTimeBlockEntity toEntity(RepTimeBlockModel model) {
        if (model == null) {
            return null;
        }

        return RepTimeBlockEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .teamMemberId(model.getTeamMemberId())
                .activityId(model.getActivityId())
                .startTime(model.getStartTime())
                .endTime(model.getEndTime())
                .blockType(stringToEnum(model.getBlockType(), RepTimeBlockType.class))
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public RepTimeBlockModel toModel(RepTimeBlockEntity entity) {
        if (entity == null) {
            return null;
        }

        return RepTimeBlockModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .teamMemberId(entity.getTeamMemberId())
                .activityId(entity.getActivityId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .blockType(enumToString(entity.getBlockType()))
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<RepTimeBlockEntity> toEntityList(List<RepTimeBlockModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
