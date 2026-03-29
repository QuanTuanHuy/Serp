/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port.write;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

public interface IProjectWritePort {
    ProjectEntity saveProject(ProjectEntity project);

    void deleteProjectById(Long id, Long tenantId);
}
