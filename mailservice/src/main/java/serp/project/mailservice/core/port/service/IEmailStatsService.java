/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.service;

import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDate;

public interface IEmailStatsService {
    /**
     * Record email send attempt
     * @param email Email entity
     * @param responseTimeMs Response time in milliseconds
     */
    void recordEmailSent(EmailEntity email, long responseTimeMs);
    
    /**
     * Record email failure
     * @param email Email entity
     */
    void recordEmailFailed(EmailEntity email);
    
    /**
     * Aggregate stats for a specific hour
     * @param tenantId Tenant ID
     * @param provider Email provider
     * @param emailType Email type
     * @param status Email status
     * @param statDate Statistics date
     * @param statHour Statistics hour (0-23)
     */
    void aggregateStats(Long tenantId, EmailProvider provider, EmailType emailType, 
                       EmailStatus status, LocalDate statDate, Integer statHour);
    
    /**
     * Clean up old statistics (keep last 90 days)
     */
    void cleanupOldStats();
}
