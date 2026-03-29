/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.priority.entity.PriorityEntity;

public interface IPriorityPort {
    PriorityEntity createPriority(PriorityEntity priority);

    Optional<PriorityEntity> getPriorityById(Long id, Long tenantId);

    Optional<PriorityEntity> getPriorityByIdIncludingSystem(Long id, Long tenantId);

    Optional<PriorityEntity> getPriorityByPriorityKey(Long tenantId, String priorityKey);

    List<PriorityEntity> listPriorities(Long tenantId);

    void updatePriority(PriorityEntity priority);

    void deletePriority(Long id, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
