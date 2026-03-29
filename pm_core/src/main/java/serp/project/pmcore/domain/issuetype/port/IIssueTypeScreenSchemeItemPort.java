/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;

public interface IIssueTypeScreenSchemeItemPort {
    List<IssueTypeScreenSchemeItemEntity> createIssueTypeScreenSchemeItems(List<IssueTypeScreenSchemeItemEntity> items);

    List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeId(Long schemeId, Long tenantId);
}
