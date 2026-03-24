package serp.project.pmcore.domain.service.provisioning.mode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.provisioning.SchemeProvisionerRegistry;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFromExistingProvisioningExecutor implements IProvisioningModeExecutor {

    private static final Set<SchemeType> SUPPORTED_SCHEMES = EnumSet.of(
            SchemeType.ISSUE_TYPE,
            SchemeType.WORKFLOW,
            SchemeType.FIELD_CONFIG,
            SchemeType.SCREEN,
            SchemeType.PERMISSION,
            SchemeType.NOTIFICATION,
            SchemeType.PRIORITY,
            SchemeType.ISSUE_SECURITY
    );

    private final SchemeProvisionerRegistry provisionerRegistry;

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.SHARED_FROM_EXISTING;
    }

    @Override
    public Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources, Long tenantId, Long userId) {
        validateArguments(resolvedSources, tenantId, userId);
        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);
        for (SchemeType schemeType : SUPPORTED_SCHEMES) {
            Long sourceSchemeId = requireSourceSchemeId(resolvedSources, schemeType);
            effectiveBindings.put(
                    schemeType,
                    provisionerRegistry.get(schemeType)
                            .resolveSharedBinding(sourceSchemeId, tenantId, userId)
            );
        }
        return effectiveBindings;
    }
}
