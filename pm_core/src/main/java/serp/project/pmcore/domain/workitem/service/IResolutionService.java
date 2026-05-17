/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;

public interface IResolutionService {
    ResolutionEntity createResolution(ResolutionEntity resolution, Long tenantId, Long userId);

    ResolutionEntity getResolutionById(Long resolutionId, Long tenantId);

    ResolutionEntity getVisibleResolutionById(Long resolutionId, Long tenantId);

    PageResult<ResolutionEntity> listVisibleResolutions(Long tenantId, ResolutionListCriteria criteria);

    ResolutionEntity updateResolution(Long resolutionId, ResolutionUpdateData data, Long tenantId, Long userId);

    ResolutionEntity deleteResolution(Long resolutionId, Long tenantId, Long userId);
}
