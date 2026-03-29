/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issyetype.port;

import java.util.Optional;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeScreenSchemeEntity;

public interface IIssueTypeScreenSchemePort {
    IssueTypeScreenSchemeEntity createIssueTypeScreenScheme(IssueTypeScreenSchemeEntity scheme);

    Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeById(Long schemeId, Long tenantId);

    Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
