/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;

public interface IResolutionPort {
    Optional<ResolutionEntity> getResolutionById(Long id, Long tenantId);

    Optional<ResolutionEntity> getResolutionByIdIncludingSystem(Long id, Long tenantId);

    Optional<ResolutionEntity> getResolutionByName(Long tenantId, String name);

    Optional<ResolutionEntity> getResolutionByNameIncludingSystem(Long tenantId, String name);

    List<ResolutionEntity> getResolutionsByTenantId(Long tenantId);

    PageResult<ResolutionEntity> listResolutionsIncludingSystem(Long tenantId, ResolutionListCriteria criteria);

    ResolutionEntity createResolution(ResolutionEntity resolution);

    void updateResolution(ResolutionEntity resolution);

    List<ResolutionEntity> createResolutions(List<ResolutionEntity> resolutions);

    boolean existsByName(Long tenantId, String name);
}
