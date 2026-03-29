package serp.project.pmcore.domain.service.provisioning.cloner;

import serp.project.pmcore.domain.shared.enums.CloneMode;

public interface ISchemeCloner<S> {
    Long cloneForTenant(S source, Long tenantId, Long userId, CloneMode mode);
}
