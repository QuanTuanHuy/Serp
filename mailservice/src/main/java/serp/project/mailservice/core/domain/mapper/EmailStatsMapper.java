/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.mapper;

import serp.project.mailservice.core.domain.dto.response.EmailStatsResponse;
import serp.project.mailservice.core.domain.entity.EmailStatsEntity;

public class EmailStatsMapper {

    public static EmailStatsResponse toResponse(EmailStatsEntity entity) {
        return EmailStatsResponse.builder()
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
                .build();
    }

    private EmailStatsMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
