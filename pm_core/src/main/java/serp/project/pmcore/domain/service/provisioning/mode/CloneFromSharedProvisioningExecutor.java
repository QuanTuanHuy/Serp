package serp.project.pmcore.domain.service.provisioning.mode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.SchemeProvisionerRegistry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloneFromSharedProvisioningExecutor implements IProvisioningModeExecutor {

    private static final List<SchemeType> SUPPORTED_SCHEMES = List.of(
            SchemeType.ISSUE_TYPE,
            SchemeType.PRIORITY,
            SchemeType.SCREEN,
            SchemeType.WORKFLOW,
            SchemeType.FIELD_CONFIG,
            SchemeType.PERMISSION,
            SchemeType.ISSUE_SECURITY,
            SchemeType.NOTIFICATION
    );

    private final SchemeProvisionerRegistry provisionerRegistry;

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.CLONE_FROM_SHARED;
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
                            .resolveClonedBinding(sourceSchemeId, tenantId, userId, context)
            );
        }
        return effectiveBindings;
    }
}
