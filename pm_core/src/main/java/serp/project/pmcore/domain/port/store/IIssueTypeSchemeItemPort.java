/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;

import java.util.List;

public interface IIssueTypeSchemeItemPort {
    IssueTypeSchemeItemEntity createIssueTypeSchemeItem(IssueTypeSchemeItemEntity item);

    List<IssueTypeSchemeItemEntity> createIssueTypeSchemeItems(List<IssueTypeSchemeItemEntity> items);

    List<IssueTypeSchemeItemEntity> getIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<IssueTypeSchemeItemEntity> getIssueTypeSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    void deleteIssueTypeSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    boolean existsIssueTypeInScheme(Long schemeId, Long issueTypeId, Long tenantId);
}
