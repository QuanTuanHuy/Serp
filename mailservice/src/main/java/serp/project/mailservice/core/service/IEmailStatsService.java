/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.entity.EmailStatsEntity;

import java.time.LocalDate;

public interface IEmailStatsService {
    void recordEmailSent(EmailEntity email, long responseTimeMs);
    
    void recordEmailFailed(EmailEntity email);
    
    void recordEmailRetry(EmailEntity email);
    
    EmailStatsEntity aggregateStats(Long tenantId, LocalDate startDate, LocalDate endDate);
    
    long cleanupOldStats(int retentionDays);
    
    double getSuccessRate(Long tenantId, LocalDate startDate, LocalDate endDate);
    
    long getAverageResponseTime(Long tenantId, LocalDate startDate, LocalDate endDate);
}
