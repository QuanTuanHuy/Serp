package serp.project.pmcore.domain.project.provisioning;

import serp.project.pmcore.domain.project.dto.ProjectProvisioningRequest;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.util.Map;

public interface ISchemeSourceResolver {
    Map<SchemeType, Long> resolve(ProjectProvisioningRequest request);
}
