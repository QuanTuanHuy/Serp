/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.serp.platform.security.context.SerpAuthContext;
import serp.project.mailservice.core.domain.dto.request.EmailStatsFilterRequest;
import serp.project.mailservice.core.domain.dto.response.EmailStatsResponse;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.ErrorCode;
import serp.project.mailservice.core.usecase.EmailStatsUseCases;
import serp.project.mailservice.kernel.utils.ResponseUtils;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/email-stats")
@Slf4j
public class EmailStatsController {

    private final EmailStatsUseCases emailStatsUseCases;
    private final SerpAuthContext authContext;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<?> getStatsByFilters(EmailStatsFilterRequest filter) {
        log.debug("Getting email stats with filters: {}", filter);

        List<EmailStatsResponse> responses = emailStatsUseCases.getStatsByFilters(filter);
        return ResponseEntity.status(200).body(responseUtils.success(responses));
    }

    @GetMapping("/aggregated")
    public ResponseEntity<?> getAggregatedStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long tenantId = authContext.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_ID_REQUIRED));

        log.debug("Getting aggregated stats for tenant: {}, from: {} to: {}", tenantId, startDate, endDate);

        EmailStatsResponse response = emailStatsUseCases.getAggregatedStats(tenantId, startDate, endDate);
        return ResponseEntity.status(200).body(responseUtils.success(response));
    }
}
