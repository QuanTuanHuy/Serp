/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.port;

import java.util.Optional;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;

public interface IIssueSecuritySchemePort {
    IssueSecuritySchemeEntity createIssueSecurityScheme(IssueSecuritySchemeEntity scheme);

    void updateIssueSecurityScheme(IssueSecuritySchemeEntity scheme);

    Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeById(Long schemeId, Long tenantId);

    Optional<IssueSecuritySchemeEntity> getIssueSecuritySchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
