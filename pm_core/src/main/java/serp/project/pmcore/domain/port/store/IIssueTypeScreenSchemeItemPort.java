/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeItemEntity;

import java.util.List;

public interface IIssueTypeScreenSchemeItemPort {
    List<IssueTypeScreenSchemeItemEntity> createIssueTypeScreenSchemeItems(List<IssueTypeScreenSchemeItemEntity> items);

    List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);
}
