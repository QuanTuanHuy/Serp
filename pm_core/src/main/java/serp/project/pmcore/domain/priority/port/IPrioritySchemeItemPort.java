/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.port;

import java.util.List;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;

public interface IPrioritySchemeItemPort {
    PrioritySchemeItemEntity createPrioritySchemeItem(PrioritySchemeItemEntity item);

    List<PrioritySchemeItemEntity> createPrioritySchemeItems(List<PrioritySchemeItemEntity> items);

    List<PrioritySchemeItemEntity> getPrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<PrioritySchemeItemEntity> getPrioritySchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    void deletePrioritySchemeItemsBySchemeId(Long schemeId, Long tenantId);

    boolean existsPriorityInScheme(Long schemeId, Long priorityId, Long tenantId);

    boolean existsByPriorityId(Long priorityId, Long tenantId);
}
