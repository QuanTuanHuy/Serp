/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemCommentModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class WorkItemCommentMapper {

    public WorkItemCommentEntity toEntity(WorkItemCommentModel model) {
        if (model == null) {
            return null;
        }
        return WorkItemCommentEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workItemId(model.getWorkItemId())
                .authorId(model.getAuthorId())
                .body(model.getBody())
                .createdAt(toEpochMilli(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(toEpochMilli(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(toEpochMilli(model.getDeletedAt()))
                .build();
    }

    public WorkItemCommentModel toModel(WorkItemCommentEntity entity) {
        return WorkItemCommentModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workItemId(entity.getWorkItemId())
                .authorId(entity.getAuthorId())
                .body(entity.getBody())
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
