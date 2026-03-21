package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.service.provisioning.materializer.IssueTypeMaterializer;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueTypeSchemeProvisioner implements ISchemeProvisioner {

    private final IssueTypeMaterializer issueTypeMaterializer;
    private final CloneNamingHelper cloneNamingHelper;

    @Override
    public SchemeType supports() {
        return SchemeType.ISSUE_TYPE;
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
