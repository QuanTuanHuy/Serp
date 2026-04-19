/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service;

import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

public interface IIssueTypeSchemeService {
    IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme, Long tenantId, Long userId);

    IssueTypeSchemeEntity getIssueTypeSchemeById(Long schemeId, Long tenantId);

    IssueTypeSchemeEntity getVisibleIssueTypeSchemeById(Long schemeId, Long tenantId);

    IssueTypeSchemeEntity getVisibleIssueTypeSchemeDetailById(Long schemeId, Long tenantId);

    PageResult<IssueTypeSchemeEntity> listVisibleIssueTypeSchemes(Long tenantId, IssueTypeSchemeListCriteria criteria);

    IssueTypeSchemeEntity updateIssueTypeScheme(Long schemeId, IssueTypeSchemeUpdateData data, Long tenantId, Long userId);

    IssueTypeSchemeEntity deleteIssueTypeScheme(Long schemeId, Long tenantId, Long userId);

    IssueTypeSchemeEntity replaceIssueTypeSchemeItems(Long schemeId, List<Long> issueTypeIds, Long tenantId, Long userId);

    void validateIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId);
}
