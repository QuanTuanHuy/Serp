/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.PrioritySchemeItemEntity;

import java.util.List;

public interface IPrioritySchemeItemPort {
    PrioritySchemeItemEntity createPrioritySchemeItem(PrioritySchemeItemEntity item);

    List<PrioritySchemeItemEntity> createPrioritySchemeItems(List<PrioritySchemeItemEntity> items);

    List<PrioritySchemeItemEntity> getPrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId);

    void deletePrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId);

    boolean existsPriorityInScheme(Long schemeId, Long priorityId, Long tenantId);
}
