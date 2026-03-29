package serp.project.pmcore.domain.project.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemePort;
import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;

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
