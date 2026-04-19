/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IIssueTypeSchemePort {
    IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeById(Long schemeId, Long tenantId);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeWithItems(Long schemeId, Long tenantId);

    List<IssueTypeSchemeEntity> listIssueTypeSchemes(Long tenantId);

    PageResult<IssueTypeSchemeEntity> listIssueTypeSchemesIncludingSystem(Long tenantId,
                                                                          IssueTypeSchemeListCriteria criteria);

    void updateIssueTypeScheme(IssueTypeSchemeEntity scheme);

    void deleteIssueTypeScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);

    boolean existsByDefaultIssueTypeId(Long issueTypeId, Long tenantId);
}
