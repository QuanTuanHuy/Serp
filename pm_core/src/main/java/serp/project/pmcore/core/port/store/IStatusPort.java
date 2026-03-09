/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.StatusEntity;

import java.util.Optional;

public interface IStatusPort {
    Optional<StatusEntity> getStatusById(Long id, Long tenantId);

    Optional<StatusEntity> getStatusByIdIncludingSystem(Long id, Long tenantId);
}
