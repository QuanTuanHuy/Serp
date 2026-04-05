/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.service;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.entity.ScreenEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;

import java.util.List;

public interface IScreenService {
    List<ScreenTabFieldEntity> getScreenTabFieldsByScreenId(Long screenId, Long tenantId);

    ScreenEntity getScreenById(Long screenId, Long tenantId);

    Long resolveScreenIdForOperation(ProjectEntity project,
                                     Long issueTypeId,
                                     String operationKey,
                                     Long tenantId);
}
