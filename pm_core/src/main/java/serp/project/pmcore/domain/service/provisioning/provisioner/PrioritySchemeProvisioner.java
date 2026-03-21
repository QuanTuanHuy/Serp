package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.enums.SchemeType;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrioritySchemeProvisioner implements ISchemeProvisioner {

    @Override
    public SchemeType supports() {
        return SchemeType.PRIORITY;
    }

    @Override
    public Long resolveSharedBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        return 0L;
    }

    @Override
    public Long resolveClonedBinding(Long sourceSchemeId, Long tenantId, Long userId) {
        return 0L;
    }
}
