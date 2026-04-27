/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeItemEntity;

public interface IIssueTypeScreenSchemeItemPort {
    List<IssueTypeScreenSchemeItemEntity> createIssueTypeScreenSchemeItems(List<IssueTypeScreenSchemeItemEntity> items);

    List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<IssueTypeScreenSchemeItemEntity> getIssueTypeScreenSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    Optional<IssueTypeScreenSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId);

    boolean existsByIssueTypeId(Long issueTypeId, Long tenantId);
}
