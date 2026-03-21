package serp.project.pmcore.domain.service.provisioning;

import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.enums.SchemeType;

import java.util.Map;

public interface ISchemeSourceResolver {
    Map<SchemeType, Long> resolve(ProjectProvisioningRequest request);
}
