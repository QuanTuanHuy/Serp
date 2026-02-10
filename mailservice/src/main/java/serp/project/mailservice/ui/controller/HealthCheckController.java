/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.mailservice.core.domain.dto.response.ProviderHealthResponse;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.usecase.HealthCheckUseCases;
import serp.project.mailservice.kernel.utils.ResponseUtils;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/health")
@Slf4j
public class HealthCheckController {

    private final HealthCheckUseCases healthCheckUseCases;
    private final ResponseUtils responseUtils;

    @GetMapping
    public ResponseEntity<?> checkAllProvidersHealth() {
        log.debug("Checking health of all providers");
        List<ProviderHealthResponse> responses = healthCheckUseCases.checkAllProviders();
        return ResponseEntity.status(200).body(responseUtils.success(responses));
    }

    @GetMapping("/providers")
    public ResponseEntity<?> getAllProvidersStatus() {
        log.debug("Getting all providers status");

        List<ProviderHealthResponse> responses = healthCheckUseCases.getProvidersStatus();
        return ResponseEntity.status(200).body(responseUtils.success(responses));
    }

    @GetMapping("/providers/{provider}")
    public ResponseEntity<?> checkProviderHealth(@PathVariable EmailProvider provider) {
        log.debug("Checking health of provider: {}", provider);

        boolean isHealthy = healthCheckUseCases.checkProviderHealth(provider);
        return ResponseEntity.status(200).body(responseUtils.success(isHealthy));
    }

    @PostMapping("/providers/{provider}/force-up")
    public ResponseEntity<?> forceProviderUp(@PathVariable EmailProvider provider) {
        log.info("Forcing provider UP: {}", provider);

        healthCheckUseCases.forceProviderUp(provider);
        return ResponseEntity.status(200).body(responseUtils.status("Provider " + provider + " marked as UP"));
    }
}
