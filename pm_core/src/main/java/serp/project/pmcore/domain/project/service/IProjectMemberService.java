/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

import java.util.List;

public interface IProjectMemberService {
    List<Long> listAssignableMembers(ProjectEntity project);
}
