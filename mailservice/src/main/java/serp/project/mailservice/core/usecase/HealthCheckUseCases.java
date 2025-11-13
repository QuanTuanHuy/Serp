/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.dto.response.ProviderHealthResponse;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.service.IEmailProviderService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckUseCases {

    private final IEmailProviderService emailProviderService;

    public List<ProviderHealthResponse> checkAllProviders() {
        log.debug("Checking health of all email providers");

        emailProviderService.checkAllProviderHealth();
        return emailProviderService.getAllProviderHealth();
    }

    public boolean checkProviderHealth(EmailProvider provider) {
        log.debug("Checking health of provider: {}", provider);

        return emailProviderService.isProviderHealthy(provider);
    }

    public void forceProviderUp(EmailProvider provider) {
        log.info("Forcing provider UP: {}", provider);

        emailProviderService.markProviderUp(provider);
    }

    public List<ProviderHealthResponse> getProvidersStatus() {
        log.debug("Getting all providers status");

        return emailProviderService.getAllProviderHealth();
    }
}
