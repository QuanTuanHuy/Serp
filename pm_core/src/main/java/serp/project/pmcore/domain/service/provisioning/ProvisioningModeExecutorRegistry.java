package serp.project.pmcore.domain.service.provisioning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainException;
import serp.project.pmcore.domain.service.provisioning.mode.IProvisioningModeExecutor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProvisioningModeExecutorRegistry {

    private final Map<ProvisioningMode, IProvisioningModeExecutor> executors;

    public ProvisioningModeExecutorRegistry(List<IProvisioningModeExecutor> executors) {
        this.executors = new EnumMap<>(ProvisioningMode.class);

        for (IProvisioningModeExecutor executor : executors) {
            ProvisioningMode mode = executor.supportsMode();
            if (mode == null) {
                throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "IProvisioningModeExecutor.supportsMode() must not return null");
            }
            IProvisioningModeExecutor existing = this.executors.put(mode, executor);
            if (existing != null) {
                throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Duplicate IProvisioningModeExecutor registered for mode: " + mode);
            }
        }
    }

    public IProvisioningModeExecutor get(ProvisioningMode mode) {
        if (mode == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Provisioning mode cannot be null");
        }
        IProvisioningModeExecutor executor = executors.get(mode);
        if (executor == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "No IProvisioningModeExecutor registered for mode: " + mode);
        }
        return executor;
    }
}
