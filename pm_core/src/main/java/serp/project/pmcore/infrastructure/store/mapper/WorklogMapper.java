/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.infrastructure.store.model.WorklogModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorklogMapper extends BaseMapper {

    public WorklogEntity toEntity(WorklogModel model) {
        if (model == null) {
            return null;
        }
        return WorklogEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workItemId(model.getWorkItemId())
                .authorId(model.getAuthorId())
                .comment(model.getComment())
                .startDate(localDateTimeToLong(model.getStartDate()))
                .timeSpent(model.getTimeSpent())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .build();
    }

    public WorklogModel toModel(WorklogEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorklogModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workItemId(entity.getWorkItemId())
                .authorId(entity.getAuthorId())
                .comment(entity.getComment())
                .startDate(longToLocalDateTime(entity.getStartDate()))
                .timeSpent(entity.getTimeSpent())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    public List<WorklogEntity> toEntities(List<WorklogModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
