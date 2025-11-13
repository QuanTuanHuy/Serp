/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.service;

import serp.project.mailservice.core.domain.dto.response.ProviderHealthResponse;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.port.client.IEmailProviderPort;

import java.util.List;

public interface IProviderHealthService {

    IEmailProviderPort getHealthyProvider();

    IEmailProviderPort getProvider(EmailProvider provider);

    void updateProviderHealth(EmailProvider provider, boolean isHealthy);

    List<ProviderHealthResponse> getAllProviderHealth();

    void checkAllProviderHealth();
}
