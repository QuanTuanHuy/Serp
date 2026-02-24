/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;

import java.util.List;
import java.util.Optional;

public interface IPrioritySchemePort {
    PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme);

    Optional<PrioritySchemeEntity> getPrioritySchemeById(Long schemeId, Long tenantId);

    Optional<PrioritySchemeEntity> getPrioritySchemeWithItems(Long schemeId, Long tenantId);

    List<PrioritySchemeEntity> listPrioritySchemes(Long tenantId);

    void updatePriorityScheme(PrioritySchemeEntity scheme);

    void deletePriorityScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
