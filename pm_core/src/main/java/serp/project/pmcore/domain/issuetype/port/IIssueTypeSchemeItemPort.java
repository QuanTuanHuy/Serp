/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;

public interface IIssueTypeSchemeItemPort {
    IssueTypeSchemeItemEntity createIssueTypeSchemeItem(IssueTypeSchemeItemEntity item);

    List<IssueTypeSchemeItemEntity> createIssueTypeSchemeItems(List<IssueTypeSchemeItemEntity> items);

    List<IssueTypeSchemeItemEntity> getIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<IssueTypeSchemeItemEntity> getIssueTypeSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    void deleteIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    boolean existsIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId);

    boolean existsByIssueTypeId(Long issueTypeId, Long tenantId);
}
