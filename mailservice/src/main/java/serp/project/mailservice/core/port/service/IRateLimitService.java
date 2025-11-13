/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.service;

import serp.project.mailservice.core.domain.enums.EmailProvider;

public interface IRateLimitService {

    boolean allowRequest(Long tenantId);

    boolean allowProviderRequest(EmailProvider provider);

    boolean allowUserRequest(Long userId);

    long getRemainingQuota(Long tenantId);
}
