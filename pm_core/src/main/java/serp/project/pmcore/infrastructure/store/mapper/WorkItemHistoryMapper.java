/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.entity.WorkItemHistoryEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemHistoryModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class WorkItemHistoryMapper {

    public WorkItemHistoryEntity toEntity(WorkItemHistoryModel model) {
        if (model == null) {
            return null;
        }
        return WorkItemHistoryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workItemId(model.getWorkItemId())
                .actorId(model.getActorId())
                .fieldKey(model.getFieldKey())
                .fieldName(model.getFieldName())
                .fromValue(model.getFromValue())
                .toValue(model.getToValue())
                .fromDisplayValue(model.getFromDisplayValue())
                .toDisplayValue(model.getToDisplayValue())
                .createdAt(toEpochMilli(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(toEpochMilli(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(toEpochMilli(model.getDeletedAt()))
                .build();
    }

    public WorkItemHistoryModel toModel(WorkItemHistoryEntity entity) {
        return WorkItemHistoryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workItemId(entity.getWorkItemId())
                .actorId(entity.getActorId())
                .fieldKey(entity.getFieldKey())
                .fieldName(entity.getFieldName())
                .fromValue(entity.getFromValue())
                .toValue(entity.getToValue())
                .fromDisplayValue(entity.getFromDisplayValue())
                .toDisplayValue(entity.getToDisplayValue())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(toLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    private Long toEpochMilli(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private LocalDateTime toLocalDateTime(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
    }
}
