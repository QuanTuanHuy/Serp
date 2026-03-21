package serp.project.pmcore.domain.service.provisioning.mode;

import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainException;
import serp.project.pmcore.domain.exception.DomainValidationException;

import java.util.Map;

public interface IProvisioningModeExecutor {
    ProvisioningMode supportsMode();
    Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources,
                                    Long tenantId,
                                    Long userId);

    default void validateArguments(Map<SchemeType, Long> resolvedSources,
                           Long tenantId,
                           Long userId) {
        if (resolvedSources == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Resolved source bindings are required");
        }
        if (tenantId == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Tenant id is required");
        }
        if (userId == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "User id is required");
        }
    }

    default Long requireSourceSchemeId(Map<SchemeType, Long> resolvedSources, SchemeType type) {
        Long sourceId = resolvedSources.get(type);
        if (sourceId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing source scheme for " + type
                            + ". Provide an explicit override, blueprint default, or tenant default/shared default."
            );
        }
        return sourceId;
    }
}
