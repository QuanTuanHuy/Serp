/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

public interface IProjectComponentPort {
    ProjectComponentEntity createComponent(ProjectComponentEntity component);

    Optional<ProjectComponentEntity> getComponentById(Long componentId, Long projectId, Long tenantId);

    List<ProjectComponentEntity> getComponentsByIds(List<Long> componentIds, Long projectId, Long tenantId);

    PageResult<ProjectComponentEntity> listComponents(Long projectId, Long tenantId, ProjectComponentListCriteria criteria);

    void updateComponent(ProjectComponentEntity component);

    boolean existsByProjectIdAndName(Long projectId, Long tenantId, String name);

    void deleteComponentLinks(Long componentId, Long tenantId);
}
