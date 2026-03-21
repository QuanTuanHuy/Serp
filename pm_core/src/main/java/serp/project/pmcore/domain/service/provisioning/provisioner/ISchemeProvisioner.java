package serp.project.pmcore.domain.service.provisioning.provisioner;

import serp.project.pmcore.domain.enums.SchemeType;

public interface ISchemeProvisioner {
    SchemeType supports();
    Long resolveSharedBinding(Long sourceSchemeId, Long tenantId, Long userId);
    Long resolveClonedBinding(Long sourceSchemeId, Long tenantId, Long userId);
}
