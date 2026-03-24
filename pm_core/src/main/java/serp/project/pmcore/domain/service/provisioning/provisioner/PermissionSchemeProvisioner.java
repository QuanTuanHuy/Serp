package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.port.store.IPermissionSchemePort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.cloner.PermissionSchemeCloner;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;

import java.util.Optional;

@Component
@Slf4j
public class PermissionSchemeProvisioner extends AbstractMappedSharedProvisioner<PermissionSchemeEntity> {

    private final IPermissionSchemePort permissionSchemePort;
    private final PermissionSchemeCloner permissionSchemeCloner;

    public PermissionSchemeProvisioner(ITenantSchemeMappingPort tenantSchemeMappingPort,
                                       IPermissionSchemePort permissionSchemePort,
                                       PermissionSchemeCloner permissionSchemeCloner) {
        super(tenantSchemeMappingPort);
        this.permissionSchemePort = permissionSchemePort;
        this.permissionSchemeCloner = permissionSchemeCloner;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.PERMISSION;
    }

    @Override
    protected Optional<PermissionSchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return permissionSchemePort.getPermissionSchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(PermissionSchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(PermissionSchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return permissionSchemePort.getPermissionSchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "permission scheme";
    }

    @Override
    protected Long cloneForTenant(PermissionSchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode cloneMode,
                                  ProvisioningExecutionContext context) {
        return permissionSchemeCloner.clonePermissionScheme(source, tenantId, userId, cloneMode);
    }
}
