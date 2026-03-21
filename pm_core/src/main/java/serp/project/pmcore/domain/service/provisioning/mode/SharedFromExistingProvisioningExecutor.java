package serp.project.pmcore.domain.service.provisioning.mode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.enums.SchemeType;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFromExistingProvisioningExecutor implements IProvisioningModeExecutor {

    @Override
    public ProvisioningMode supportsMode() {
        return ProvisioningMode.SHARED_FROM_EXISTING;
    }

    @Override
    public Map<SchemeType, Long> provision(Map<SchemeType, Long> resolvedSources, Long tenantId, Long userId) {
        return Map.of();
    }
}
