/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;

public interface IResolutionPort {
    Optional<ResolutionEntity> getResolutionById(Long id, Long tenantId);

    Optional<ResolutionEntity> getResolutionByIdIncludingSystem(Long id, Long tenantId);

    Optional<ResolutionEntity> getResolutionByName(Long tenantId, String name);

    List<ResolutionEntity> getResolutionsByTenantId(Long tenantId);

    ResolutionEntity createResolution(ResolutionEntity resolution);

    List<ResolutionEntity> createResolutions(List<ResolutionEntity> resolutions);
}
