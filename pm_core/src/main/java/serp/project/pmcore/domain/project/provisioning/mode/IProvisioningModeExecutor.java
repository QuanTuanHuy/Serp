package serp.project.pmcore.domain.project.provisioning.mode;

import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.shared.enums.ProvisioningMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;

import java.util.Map;
import java.util.Objects;

public interface IProvisioningModeExecutor {
    ProvisioningMode supportsMode();

    Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources,
                                    Long tenantId,
                                    Long userId,
                                    ProvisioningExecutionContext context);

    default void validateArguments(Map<SchemeType, Long> resolvedSources,
                                   Long tenantId,
                                   Long userId,
                                   ProvisioningExecutionContext context) {
        Objects.requireNonNull(resolvedSources, "resolvedSources must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(context, "context must not be null");
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
