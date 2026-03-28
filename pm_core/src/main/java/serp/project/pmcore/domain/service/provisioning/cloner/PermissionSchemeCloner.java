package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.port.store.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.port.store.IPermissionSchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionSchemeCloner {

    private final IPermissionSchemePort permissionSchemePort;
    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;
    private final CloneNamingHelper cloneNamingHelper;

    public Long clonePermissionScheme(PermissionSchemeEntity source,
                                      Long tenantId,
                                      Long userId,
                                      CloneMode cloneMode,
                                      ProvisioningExecutionContext context) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<PermissionSchemeEntryEntity> sourceEntries = permissionSchemeEntryPort
                .getPermissionSchemeEntriesBySchemeIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();

        PermissionSchemeEntity cloned = PermissionSchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.PERMISSION, cloneMode))
                .description(source.getDescription())
                .build();
        cloned.applyCreate(userId, now);
        PermissionSchemeEntity saved = permissionSchemePort.createPermissionScheme(cloned);

        if (!sourceEntries.isEmpty()) {
            List<PermissionSchemeEntryEntity> clonedEntries = new ArrayList<>();
            for (PermissionSchemeEntryEntity entry : sourceEntries) {
                clonedEntries.add(PermissionSchemeEntryEntity.builder()
                        .tenantId(tenantId)
                        .schemeId(saved.getId())
                        .permissionKey(entry.getPermissionKey())
                        .granteeType(entry.getGranteeType())
                        .granteeRef(entry.getGranteeRef())
                        .customFieldId(entry.getCustomFieldId())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }
            permissionSchemeEntryPort.createPermissionSchemeEntries(clonedEntries);
        }

        log.info("Created {} PERMISSION scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    fieldName + " is required"
            );
        }
    }
}