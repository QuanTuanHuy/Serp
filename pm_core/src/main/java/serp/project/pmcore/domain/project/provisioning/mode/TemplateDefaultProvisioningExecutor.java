package serp.project.pmcore.domain.project.provisioning.mode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.SchemeProvisionerRegistry;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateDefaultProvisioningExecutor implements IProvisioningModeExecutor {

    private static final List<SchemeType> CLONED_SCHEME_TYPES = List.of(
            SchemeType.ISSUE_TYPE,
            SchemeType.SCREEN,
            SchemeType.WORKFLOW,
            SchemeType.FIELD_CONFIG
    );

    private static final List<SchemeType> SHARED_SCHEME_TYPES = List.of(
            SchemeType.PERMISSION,
            SchemeType.ISSUE_SECURITY,
            SchemeType.NOTIFICATION,
            SchemeType.PRIORITY
    );

    private final SchemeProvisionerRegistry provisionerRegistry;

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.TEMPLATE_DEFAULT;
    }

    @Override
    public Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources,
                                           Long tenantId,
                                           Long userId,
                                           ProvisioningExecutionContext context) {
        validateArguments(resolvedSources, tenantId, userId, context);

        Map<SchemeType, Long> effectiveBindings = new EnumMap<>(SchemeType.class);

        provisionGroup(
                resolvedSources,
                tenantId,
                userId,
                effectiveBindings,
                CLONED_SCHEME_TYPES,
                (schemeType, sourceSchemeId) -> provisionerRegistry.get(schemeType)
                        .resolveClonedBinding(sourceSchemeId, tenantId, userId, context)
        );

        provisionGroup(
                resolvedSources,
                tenantId,
                userId,
                effectiveBindings,
                SHARED_SCHEME_TYPES,
                (schemeType, sourceSchemeId) -> provisionerRegistry.get(schemeType)
                        .resolveSharedBinding(sourceSchemeId, tenantId, userId, context)
        );

        return effectiveBindings;
    }

    private void provisionGroup(Map<SchemeType, Long> resolvedSources,
                                Long tenantId,
                                Long userId,
                                Map<SchemeType, Long> effectiveBindings,
                                List<SchemeType> schemeTypes,
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
