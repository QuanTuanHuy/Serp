/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service;

import java.util.List;

import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IIssueTypeService {
    IssueTypeEntity createIssueType(IssueTypeEntity issueType, Long tenantId, Long userId);

    IssueTypeEntity getIssueTypeById(Long issueTypeId, Long tenantId);

    IssueTypeEntity getVisibleIssueTypeById(Long issueTypeId, Long tenantId);

    List<IssueTypeEntity> getVisibleIssueTypesByIds(List<Long> issueTypeIds, Long tenantId);

    PageResult<IssueTypeEntity> listVisibleIssueTypes(Long tenantId, IssueTypeListCriteria criteria);

    IssueTypeEntity updateIssueType(Long issueTypeId, IssueTypeUpdateData data, Long tenantId, Long userId);

    IssueTypeEntity deleteIssueType(Long issueTypeId, Long tenantId, Long userId);
}
