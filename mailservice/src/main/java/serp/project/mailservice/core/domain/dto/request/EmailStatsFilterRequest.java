/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.request;

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
public class EmailStatsFilterRequest {
    private Long tenantId;
    private EmailProvider provider;
    private EmailType emailType;
    private EmailStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String granularity;
}
