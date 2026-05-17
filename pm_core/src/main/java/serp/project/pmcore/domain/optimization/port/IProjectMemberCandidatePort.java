/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

import java.util.List;

public interface IProjectMemberCandidatePort {
    List<Long> listAssignableMembers(ProjectEntity project);
}
