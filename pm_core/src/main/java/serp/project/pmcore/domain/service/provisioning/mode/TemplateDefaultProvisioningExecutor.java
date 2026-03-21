package serp.project.pmcore.domain.service.provisioning.mode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.provisioning.SchemeProvisionerRegistry;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateDefaultProvisioningExecutor implements IProvisioningModeExecutor {

    private static final Set<SchemeType> CLONED_SCHEME_TYPES = EnumSet.of(
            SchemeType.ISSUE_TYPE,
            SchemeType.WORKFLOW,
            SchemeType.FIELD_CONFIG,
            SchemeType.SCREEN
    );

    private static final Set<SchemeType> SHARED_SCHEME_TYPES = EnumSet.of(
            SchemeType.PERMISSION,
            SchemeType.NOTIFICATION,
            SchemeType.PRIORITY,
            SchemeType.ISSUE_SECURITY
    );

    private final SchemeProvisionerRegistry provisionerRegistry;

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.TEMPLATE_DEFAULT;
    }

    @Override
    public Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources,
                                           Long tenantId,
                                           Long userId) {
        validateArguments(resolvedSources, tenantId, userId);

        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);

        provisionGroup(
                resolvedSources,
                tenantId,
                userId,
                effectiveBindings,
                CLONED_SCHEME_TYPES,
                (schemeType, sourceSchemeId) -> provisionerRegistry.get(schemeType)
                        .resolveClonedBinding(sourceSchemeId, tenantId, userId)
        );

        provisionGroup(
                resolvedSources,
                tenantId,
                userId,
                effectiveBindings,
                SHARED_SCHEME_TYPES,
                (schemeType, sourceSchemeId) -> provisionerRegistry.get(schemeType)
                        .resolveSharedBinding(sourceSchemeId, tenantId, userId)
        );

        return effectiveBindings;
    }

    private void provisionGroup(Map<SchemeType, Long> resolvedSources,
                                Long tenantId,
                                Long userId,
                                Map<SchemeType, Long> effectiveBindings,
                                Set<SchemeType> schemeTypes,
                                BiFunction<SchemeType, Long, Long> resolver) {
        for (SchemeType schemeType : schemeTypes) {
            Long sourceId = requireSourceSchemeId(resolvedSources, schemeType);
            Long effectiveId = resolver.apply(schemeType, sourceId);
            effectiveBindings.put(schemeType, effectiveId);

            log.debug("Provisioned TEMPLATE_DEFAULT binding: tenantId={}, userId={}, schemeType={}, sourceId={}, effectiveId={}",
                    tenantId, userId, schemeType, sourceId, effectiveId);
        }
    }
}
