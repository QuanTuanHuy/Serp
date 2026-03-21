package serp.project.pmcore.domain.service.provisioning.provisioner;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.enums.SchemeType;

@Component
public class WorkflowSchemeProvisioner implements ISchemeProvisioner {

    @Override
    public SchemeType supports() {
        return SchemeType.WORKFLOW;
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
