/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.dto.response.ProviderHealthResponse;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;

import java.time.Duration;
import java.util.List;

public interface IEmailProviderService {
    IEmailProviderPort selectProvider(EmailEntity email);
    
    void markProviderDown(EmailProvider provider, Duration downtime);
    
    void markProviderUp(EmailProvider provider);
    
    boolean isProviderHealthy(EmailProvider provider);
    
    IEmailProviderPort getProvider(EmailProvider providerType);
    
    void checkAllProviderHealth();
    
    IEmailProviderPort getHealthyProvider();
    
    void updateProviderHealth(EmailProvider provider, boolean isHealthy);
    
    List<ProviderHealthResponse> getAllProviderHealth();
}
