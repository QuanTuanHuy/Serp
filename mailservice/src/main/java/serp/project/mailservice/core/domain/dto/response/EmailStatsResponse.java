/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatsResponse {
    private Long id;
    private Long tenantId;
    private EmailProvider provider;
    private EmailType emailType;
    private EmailStatus status;

    private LocalDate statDate;
    private Integer statHour;

    private Long totalCount;
    private Long successCount;
    private Long failedCount;
    private Long retryCount;

    private Long avgResponseTimeMs;
    private Long minResponseTimeMs;
    private Long maxResponseTimeMs;

    private Long totalSizeBytes;
    private Long attachmentCount;
}
