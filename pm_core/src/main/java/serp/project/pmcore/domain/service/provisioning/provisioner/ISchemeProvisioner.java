package serp.project.pmcore.domain.service.provisioning.provisioner;

import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.shared.enums.SchemeType;

public interface ISchemeProvisioner {
    SchemeType supports();
    Long resolveSharedBinding(Long sourceSchemeId, Long tenantId, Long userId, ProvisioningExecutionContext context);
    Long resolveClonedBinding(Long sourceSchemeId, Long tenantId, Long userId, ProvisioningExecutionContext context);
}
