/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IIssueTypePort {
    IssueTypeEntity createIssueType(IssueTypeEntity issueType);

    Optional<IssueTypeEntity> getIssueTypeById(Long issueTypeId, Long tenantId);

    Optional<IssueTypeEntity> getIssueTypeByIdIncludingSystem(Long issueTypeId, Long tenantId);

    Optional<IssueTypeEntity> getIssueTypeByTypeKey(Long tenantId, String typeKey);

    Optional<IssueTypeEntity> getIssueTypeByTypeKeyIncludingSystem(Long tenantId, String typeKey);

    List<IssueTypeEntity> listIssueTypes(Long tenantId);

    List<IssueTypeEntity> listIssueTypesIncludingSystem(Long tenantId);

    List<IssueTypeEntity> getIssueTypesByIdsIncludingSystem(List<Long> issueTypeIds, Long tenantId);

    PageResult<IssueTypeEntity> listIssueTypesIncludingSystem(Long tenantId, IssueTypeListCriteria criteria);

    void updateIssueType(IssueTypeEntity issueType);

    void deleteIssueType(Long issueTypeId, Long tenantId);

    boolean existsByTypeKey(Long tenantId, String typeKey);
}
