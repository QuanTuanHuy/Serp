/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.PriorityEntity;

import java.util.List;
import java.util.Optional;

public interface IPriorityPort {
    PriorityEntity createPriority(PriorityEntity priority);

    Optional<PriorityEntity> getPriorityById(Long id, Long tenantId);

    List<PriorityEntity> listPriorities(Long tenantId);

    void updatePriority(PriorityEntity priority);

    void deletePriority(Long id, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
