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
import serp.project.mailservice.core.domain.enums.ProviderStatus;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderHealthResponse {
    private EmailProvider provider;
    private ProviderStatus status;
    private Long successRate;
    private Long avgResponseTimeMs;
    private Long failedCount;
    private LocalDateTime lastCheckAt;
    private String errorMessage;
}
