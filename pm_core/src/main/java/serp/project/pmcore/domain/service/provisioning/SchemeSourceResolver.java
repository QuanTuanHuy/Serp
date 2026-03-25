package serp.project.pmcore.domain.service.provisioning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.entity.TenantSchemeDefaultEntity;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainException;
import serp.project.pmcore.domain.port.store.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.domain.port.store.ITenantSchemeDefaultPort;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemeSourceResolver implements ISchemeSourceResolver {

    private final IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;
    private final ITenantSchemeDefaultPort tenantSchemeDefaultPort;

    @Override
    public Map<SchemeType, Long> resolve(ProjectProvisioningRequest request) {
        Map<SchemeType, Long> overrides = request.getRequestedSchemeBindings() != null ?
                request.getRequestedSchemeBindings().toSchemeMap() :
                Collections.emptyMap();

        Map<SchemeType, Long> blueprintDefaults = loadBlueprintDefaults(request.getBlueprintId(), request.getTenantId());
        Map<SchemeType, Long> tenantDefaults = loadTenantDefaults(request.getTenantId());

        return resolve(overrides, blueprintDefaults, tenantDefaults);
    }

    private Map<SchemeType, Long> resolve(Map<SchemeType, Long> overrides,
                                          Map<SchemeType, Long> blueprintDefaults,
                                          Map<SchemeType, Long> tenantDefaults) {
        Map<SchemeType, Long> resolve = new EnumMap<>(SchemeType.class);
        for (SchemeType type : SchemeType.values()) {
            if (overrides != null && overrides.containsKey(type)) {
                resolve.put(type, overrides.get(type));
                continue;
            }
            if (blueprintDefaults.containsKey(type)) {
                resolve.put(type, blueprintDefaults.get(type));
                continue;
            }
            if (tenantDefaults.containsKey(type)) {
                resolve.put(type, tenantDefaults.get(type));
                continue;
            }
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing source scheme for " + type + ". Please specify in request, or set a default in blueprint or tenant settings.");
        }
        return resolve;
    }

    private Map<SchemeType, Long> loadBlueprintDefaults(Long blueprintId, Long tenantId) {
        if (blueprintId == null) {
            return Collections.emptyMap();
        }
        List<BlueprintSchemeDefaultEntity> defaults = blueprintSchemeDefaultPort
                .getDefaultsByBlueprintIdIncludingSystem(blueprintId, tenantId);
        Map<SchemeType, BlueprintSchemeDefaultEntity> preferredDefault = new EnumMap<>(SchemeType.class);
        for (BlueprintSchemeDefaultEntity candidate : defaults) {
            if (candidate.getSchemeType() == null || candidate.getSchemeId() == null) {
                continue;
            }
            var existing = preferredDefault.get(candidate.getSchemeType());
            if (shouldReplaceDefault(existing == null ? null : existing.getTenantId(), candidate.getTenantId(), tenantId)) {
                preferredDefault.put(candidate.getSchemeType(), candidate);
            }
        }
        return preferredDefault.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getSchemeId()
                ));
    }

    private Map<SchemeType, Long> loadTenantDefaults(Long tenantId) {
        List<TenantSchemeDefaultEntity> defaults = tenantSchemeDefaultPort.getDefaultsByTenantIdIncludingSystem(tenantId);
        return defaults.stream()
                .filter(d -> d.getSchemeType() != null && d.getSchemeId() != null)
                .collect(Collectors.toMap(
                        TenantSchemeDefaultEntity::getSchemeType,
                        TenantSchemeDefaultEntity::getSchemeId,
                        (existing, candidate) -> existing.equals(candidate) ? existing : candidate
                ));
    }

    private boolean shouldReplaceDefault(Long existingTenantId, Long candidateTenantId, Long tenantId) {
        if (existingTenantId == null) {
            return true;
        }

        return tenantId.equals(candidateTenantId) && !tenantId.equals(existingTenantId);
    }
}
