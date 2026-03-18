/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.IssueTypeScreenSchemeEntity;

import java.util.Optional;

public interface IIssueTypeScreenSchemePort {
    Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeById(Long schemeId, Long tenantId);

    Optional<IssueTypeScreenSchemeEntity> getIssueTypeScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
