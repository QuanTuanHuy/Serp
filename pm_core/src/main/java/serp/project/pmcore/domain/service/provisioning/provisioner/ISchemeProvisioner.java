package serp.project.pmcore.domain.service.provisioning.provisioner;

import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;

public interface ISchemeProvisioner {
    SchemeType supports();
    Long resolveSharedBinding(Long sourceSchemeId, Long tenantId, Long userId, ProvisioningExecutionContext context);
    Long resolveClonedBinding(Long sourceSchemeId, Long tenantId, Long userId, ProvisioningExecutionContext context);
}
