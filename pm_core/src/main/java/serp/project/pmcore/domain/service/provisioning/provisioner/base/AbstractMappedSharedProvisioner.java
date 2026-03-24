package serp.project.pmcore.domain.service.provisioning.provisioner.base;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.constant.TenantConstants;
import serp.project.pmcore.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.provisioner.ISchemeProvisioner;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMappedSharedProvisioner<S> implements ISchemeProvisioner {

    private final ITenantSchemeMappingPort tenantSchemeMappingPort;

    @Override
    public Long resolveSharedBinding(Long sourceSchemeId,
                                     Long tenantId,
                                     Long userId,
                                     ProvisioningExecutionContext context) {
        validateCommonArguments(sourceSchemeId, tenantId, userId);

        S source = loadRequiredSource(sourceSchemeId, tenantId);

        if (getSourceTenantId(source).equals(tenantId)) {
            return getSourceId(source);
        }

        if (!TenantConstants.SYSTEM_TENANT_ID.equals(getSourceTenantId(source))) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_NOT_FOUND,
                    "Source " + sourceEntityLabel()
                            + " is not tenant/system scoped: schemeType=" + supports()
                            + ", sourceSchemeId=" + sourceSchemeId
            );
        }

        Optional<TenantSchemeMappingEntity> existingMapping = tenantSchemeMappingPort
                .getMapping(tenantId, supports(), getSourceId(source));

        if (existingMapping.isPresent()) {
            Long mappedSchemeId = existingMapping.get().getTenantSchemeId();
            if (mappedSchemeId != null && tenantSchemeExists(mappedSchemeId, tenantId)) {
                return mappedSchemeId;
            }
            log.warn("Stale {} mapping found (tenantId={}, sourceSchemeId={}, mappedSchemeId={})",
                    supports(), tenantId, sourceSchemeId, mappedSchemeId);
        }

        Long clonedSchemeId = cloneForTenant(source, tenantId, userId, CloneMode.SHARED, context);
        upsertMapping(existingMapping, tenantId, userId, sourceSchemeId, clonedSchemeId);

        return clonedSchemeId;
    }

    @Override
    public Long resolveClonedBinding(Long sourceSchemeId,
                                     Long tenantId,
                                     Long userId,
                                     ProvisioningExecutionContext context) {
        validateCommonArguments(sourceSchemeId, tenantId, userId);
        S source = loadRequiredSource(sourceSchemeId, tenantId);
        return cloneForTenant(source, tenantId, userId, CloneMode.CLONE, context);
    }

    protected S loadRequiredSource(Long sourceSchemeId, Long tenantId) {
        return loadSourceByIdIncludingSystem(sourceSchemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCHEME_NOT_FOUND,
                        "Source " + sourceEntityLabel()
                        + " not found: schemeType=" + supports()
                        + ", sourceSchemeId=" + sourceSchemeId
                ));
    }

    protected void upsertMapping(Optional<TenantSchemeMappingEntity> existing,
                                 Long tenantId,
                                 Long userId,
                                 Long sourceSchemeId,
                                 Long tenantSchemeId) {
        long now = System.currentTimeMillis();

        TenantSchemeMappingEntity mapping = existing.orElseGet(() ->
                TenantSchemeMappingEntity.builder()
                        .tenantId(tenantId)
                        .schemeType(supports())
                        .sourceSchemeId(sourceSchemeId)
                        .createdAt(now)
                        .createdBy(userId)
                        .build()
        );

        mapping.setTenantSchemeId(tenantSchemeId);
        mapping.setUpdatedAt(now);
        mapping.setUpdatedBy(userId);

        tenantSchemeMappingPort.saveMapping(mapping);
    }


    protected void validateCommonArguments(Long sourceSchemeId, Long tenantId, Long userId) {
        Objects.requireNonNull(sourceSchemeId, "Source scheme id is required");
        Objects.requireNonNull(tenantId, "Tenant id is required");
        Objects.requireNonNull(userId, "User id is required");
    }

    /**
     * Load source scheme/root including both tenant-owned and system-owned records.
     */
    protected abstract Optional<S> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId);

    /**
     * Return source/root id.
     */
    protected abstract Long getSourceId(S source);

    /**
     * Return tenant ownership of source/root.
     */
    protected abstract Long getSourceTenantId(S source);

    /**
     * Validate whether the mapped tenant scheme still exists.
     */
    protected abstract boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId);

    /**
     * Deep-clone source/root into tenant scope.
     */
    protected abstract Long cloneForTenant(S source,
                                           Long tenantId,
                                           Long userId,
                                           CloneMode mode,
                                           ProvisioningExecutionContext context);

    /**
     * Used only for clearer exception/log messages.
     */
    protected String sourceEntityLabel() {
        return "scheme";
    }
}
