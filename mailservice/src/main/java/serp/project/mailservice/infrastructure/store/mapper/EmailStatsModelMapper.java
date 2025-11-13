/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.mapper;

import serp.project.mailservice.core.domain.entity.EmailStatsEntity;
import serp.project.mailservice.infrastructure.store.model.EmailStatsModel;

import java.util.List;
import java.util.stream.Collectors;

public class EmailStatsModelMapper {
    
    public static EmailStatsEntity toEntity(EmailStatsModel model) {
        if (model == null) {
            return null;
        }
        
        return EmailStatsEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .provider(model.getProvider())
                .emailType(model.getEmailType())
                .status(model.getStatus())
                .statDate(model.getStatDate())
                .statHour(model.getStatHour())
                .totalCount(model.getTotalCount())
                .successCount(model.getSuccessCount())
                .failedCount(model.getFailedCount())
                .retryCount(model.getRetryCount())
                .avgResponseTimeMs(model.getAvgResponseTimeMs())
                .minResponseTimeMs(model.getMinResponseTimeMs())
                .maxResponseTimeMs(model.getMaxResponseTimeMs())
                .totalSizeBytes(model.getTotalSizeBytes())
                .attachmentCount(model.getAttachmentCount())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
    
    public static EmailStatsModel toModel(EmailStatsEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return EmailStatsModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .provider(entity.getProvider())
                .emailType(entity.getEmailType())
                .status(entity.getStatus())
                .statDate(entity.getStatDate())
                .statHour(entity.getStatHour())
                .totalCount(entity.getTotalCount())
                .successCount(entity.getSuccessCount())
                .failedCount(entity.getFailedCount())
                .retryCount(entity.getRetryCount())
                .avgResponseTimeMs(entity.getAvgResponseTimeMs())
                .minResponseTimeMs(entity.getMinResponseTimeMs())
                .maxResponseTimeMs(entity.getMaxResponseTimeMs())
                .totalSizeBytes(entity.getTotalSizeBytes())
                .attachmentCount(entity.getAttachmentCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public static List<EmailStatsEntity> toEntities(List<EmailStatsModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(EmailStatsModelMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<EmailStatsModel> toModels(List<EmailStatsEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(EmailStatsModelMapper::toModel)
                .collect(Collectors.toList());
    }
    
    private EmailStatsModelMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
