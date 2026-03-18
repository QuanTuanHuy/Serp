/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;

import java.util.Optional;

public interface IIssueSecuritySchemePort {
    Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeById(Long schemeId, Long tenantId);

    Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
