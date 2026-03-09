/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.IssueTypeSchemeEntity;

import java.util.List;
import java.util.Optional;

public interface IIssueTypeSchemePort {
    IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeById(Long schemeId, Long tenantId);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<IssueTypeSchemeEntity> getIssueTypeSchemeWithItems(Long schemeId, Long tenantId);

    List<IssueTypeSchemeEntity> listIssueTypeSchemes(Long tenantId);

    void updateIssueTypeScheme(IssueTypeSchemeEntity scheme);

    void deleteIssueTypeScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
