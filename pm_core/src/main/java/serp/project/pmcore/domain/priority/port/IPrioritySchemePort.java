/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

public interface IPrioritySchemePort {
    PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme);

    Optional<PrioritySchemeEntity> getPrioritySchemeById(Long schemeId, Long tenantId);

    Optional<PrioritySchemeEntity> getPrioritySchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<PrioritySchemeEntity> getPrioritySchemeWithItems(Long schemeId, Long tenantId);

    List<PrioritySchemeEntity> listPrioritySchemes(Long tenantId);

    void updatePriorityScheme(PrioritySchemeEntity scheme);

    void deletePriorityScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
