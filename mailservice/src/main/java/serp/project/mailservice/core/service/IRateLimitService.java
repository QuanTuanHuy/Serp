/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.enums.EmailProvider;

public interface IRateLimitService {
    boolean allowRequest(Long tenantId);
    
    boolean allowProviderRequest(EmailProvider provider);
    
    boolean allowUserRequest(Long userId);
    
    long getRemainingQuota(Long tenantId);
    
    long getProviderRemainingQuota(EmailProvider provider);
    
    void resetTenantRateLimit(Long tenantId);
    
    void resetProviderRateLimit(EmailProvider provider);
}
