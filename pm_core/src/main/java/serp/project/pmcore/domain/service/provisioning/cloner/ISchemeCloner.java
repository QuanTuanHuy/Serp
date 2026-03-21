package serp.project.pmcore.domain.service.provisioning.cloner;

import serp.project.pmcore.domain.enums.CloneMode;

public interface ISchemeCloner<S> {
    Long cloneForTenant(S source, Long tenantId, Long userId, CloneMode mode);
}
