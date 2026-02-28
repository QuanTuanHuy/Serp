/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.infrastructure.store.model.OutboxEventModel;

@Component
public class OutboxEventMapper extends BaseMapper {
    public OutboxEventEntity toEntity(OutboxEventModel model) {
        if (model == null) { return null; }
        return OutboxEventEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .aggregateType(model.getAggregateType())
                .aggregateId(model.getAggregateId())
                .eventType(model.getEventType())
                .topic(model.getTopic())
                .partitionKey(model.getPartitionKey())
                .payload(model.getPayload())
                .status(model.getStatus())
                .retryCount(model.getRetryCount())
                .maxRetries(model.getMaxRetries())
                .nextRetryAt(localDateTimeToLong(model.getNextRetryAt()))
                .publishedAt(localDateTimeToLong(model.getPublishedAt()))
                .errorMessage(model.getErrorMessage())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public OutboxEventModel toModel(OutboxEventEntity entity) {
        if (entity == null) { return null; }
        return OutboxEventModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .eventType(entity.getEventType())
                .topic(entity.getTopic())
                .partitionKey(entity.getPartitionKey())
                .payload(entity.getPayload())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .nextRetryAt(longToLocalDateTime(entity.getNextRetryAt()))
                .publishedAt(longToLocalDateTime(entity.getPublishedAt()))
                .errorMessage(entity.getErrorMessage())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<OutboxEventEntity> toEntities(List<OutboxEventModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }
}
