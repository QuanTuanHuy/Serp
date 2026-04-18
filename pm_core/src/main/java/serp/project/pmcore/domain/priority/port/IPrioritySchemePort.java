/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IPrioritySchemePort {
    PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme);

    Optional<PrioritySchemeEntity> getPrioritySchemeById(Long schemeId, Long tenantId);

    Optional<PrioritySchemeEntity> getPrioritySchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<PrioritySchemeEntity> getPrioritySchemeWithItems(Long schemeId, Long tenantId);

    List<PrioritySchemeEntity> listPrioritySchemes(Long tenantId);

    PageResult<PrioritySchemeEntity> listPrioritySchemesIncludingSystem(Long tenantId,
                                                                        PrioritySchemeListCriteria criteria);

    void updatePriorityScheme(PrioritySchemeEntity scheme);

    void deletePriorityScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);

    boolean existsByDefaultPriorityId(Long priorityId, Long tenantId);
}
