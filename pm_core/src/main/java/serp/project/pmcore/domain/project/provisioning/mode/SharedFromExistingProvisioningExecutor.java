package serp.project.pmcore.domain.project.provisioning.mode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.SchemeProvisionerRegistry;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFromExistingProvisioningExecutor implements IProvisioningModeExecutor {

    private static final List<SchemeType> SUPPORTED_SCHEMES = List.of(
            SchemeType.ISSUE_TYPE,
            SchemeType.SCREEN,
            SchemeType.WORKFLOW,
            SchemeType.FIELD_CONFIG,
            SchemeType.PERMISSION,
            SchemeType.ISSUE_SECURITY,
            SchemeType.NOTIFICATION,
            SchemeType.PRIORITY
    );

    private final SchemeProvisionerRegistry provisionerRegistry;

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.SHARED_FROM_EXISTING;
    }

    @Override
    public Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources,
                                           Long tenantId,
                                           Long userId,
                                           ProvisioningExecutionContext context) {
        validateArguments(resolvedSources, tenantId, userId, context);
        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);
        for (SchemeType schemeType : SUPPORTED_SCHEMES) {
            Long sourceSchemeId = requireSourceSchemeId(resolvedSources, schemeType);
            effectiveBindings.put(
                    schemeType,
                    provisionerRegistry.get(schemeType)
                            .resolveSharedBinding(sourceSchemeId, tenantId, userId, context)
            );
        }
        return effectiveBindings;
    }
}
