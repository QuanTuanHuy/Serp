/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IProjectComponentService {
    ProjectComponentEntity createComponent(ProjectComponentEntity component, Long tenantId, Long userId);

    ProjectComponentEntity getComponentById(Long componentId, Long projectId, Long tenantId);

    java.util.List<ProjectComponentEntity> getComponentsByIds(java.util.List<Long> componentIds, Long projectId, Long tenantId);

    PageResult<ProjectComponentEntity> listComponents(Long projectId, Long tenantId, ProjectComponentListCriteria criteria);

    ProjectComponentEntity updateComponent(Long componentId,
                                           Long projectId,
                                           ProjectComponentUpdateData data,
                                           Long tenantId,
                                           Long userId);

    ProjectComponentEntity deleteComponent(Long componentId, Long projectId, Long tenantId, Long userId);
}
