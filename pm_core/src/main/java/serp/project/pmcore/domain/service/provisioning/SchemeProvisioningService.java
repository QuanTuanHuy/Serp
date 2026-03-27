/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.provisioning;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningResult;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectSchemeBindings;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.service.provisioning.mode.IProvisioningModeExecutor;

import java.util.*;

/**
 * Handles scheme provisioning for project creation.
 * <p>
 * Current phase:
 * - resolve source schemes by precedence: explicit override -> blueprint default -> tenant default/shared default
 * - keep create-project orchestration routed through a typed provisioning contract
 * - materialize supported system-owned sources to tenant scope for ISSUE_TYPE/PRIORITY/WORKFLOW
 * - enforce tenant-owned bindings or pre-materialized mapping reuse for other scheme families
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeProvisioningService implements ISchemeProvisioningService {

    private final ProvisioningModeExecutorRegistry provisioningModeExecutorRegistry;
    private final ISchemeSourceResolver schemeSourceResolver;

    @Override
    public ProjectProvisioningResult provisionProjectSchemes(ProjectEntity project,
                                                             @Valid ProjectProvisioningRequest request) {
        ProvisioningExecutionContext context = ProvisioningExecutionContext.builder()
                .projectId(request.getProjectId() != null ? request.getProjectId() : project.getId())
                .projectKey(request.getProjectKey() != null ? request.getProjectKey() : project.getKey())
                .build();

        Map<SchemeType, Long> resolvedSources = schemeSourceResolver.resolve(request);
        log.info("Resolved sources for project key={} resolvedSources={}", project.getKey(), resolvedSources);

        Map<SchemeType, Long> effectiveBindings = provisionEffectiveBindings(
                project,
                resolvedSources,
                request.getTenantId(),
                request.getUserId(),
                request.getEffectiveProvisioningMode(),
                context
        );
        log.info("Provisioned effective bindings for project key={} effectiveBindings={}", project.getKey(), effectiveBindings);

        return ProjectProvisioningResult.builder()
                .resolvedSourceBindings(ProjectSchemeBindings.fromSchemeMap(resolvedSources))
                .effectiveBindings(ProjectSchemeBindings.fromSchemeMap(effectiveBindings))
                .build();
    }

    private Map<SchemeType, Long> provisionEffectiveBindings(ProjectEntity project,
                                                             Map<SchemeType, Long> resolvedSources,
                                                             Long tenantId,
                                                             Long userId,
                                                             ProvisioningMode provisioningMode,
                                                             ProvisioningExecutionContext context) {
        IProvisioningModeExecutor executor = provisioningModeExecutorRegistry.get(provisioningMode);
        Map<SchemeType, Long> effectiveBindings = executor.provision(resolvedSources, tenantId, userId, context);

        log.info("Provisioned schemes for project key={} mode={} resolvedSources={} effectiveBindings={}",
                project.getKey(), provisioningMode, resolvedSources, effectiveBindings);
        return effectiveBindings;
    }


}
