/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.service;

import serp.project.mailservice.core.domain.entity.EmailEntity;

public interface IRetryService {
    /**
     * Calculate next retry time using exponential backoff
     * @param retryCount Current retry count
     * @return Next retry time in milliseconds
     */
    long calculateNextRetryTime(int retryCount);
    
    /**
     * Check if email should be retried
     * @param email Email entity
     * @return true if email should be retried
     */
    boolean shouldRetry(EmailEntity email);
    
    /**
     * Update email for retry
     * @param email Email entity
     */
    void scheduleRetry(EmailEntity email);
    
    /**
     * Process retry queue
     */
    void processRetryQueue();
}
