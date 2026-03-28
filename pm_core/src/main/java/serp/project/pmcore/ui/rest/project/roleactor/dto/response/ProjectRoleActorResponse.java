/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.roleactor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRoleActorResponse {
    private Long id;
    private Long projectId;
    private Long projectRoleId;
    private String subjectType;
    private String subjectId;
    private Long createdAt;
    private Long createdBy;

    public static ProjectRoleActorResponse from(ProjectRoleActorView entity) {
        return ProjectRoleActorResponse.builder()
                .id(entity.id())
                .projectId(entity.projectId())
                .projectRoleId(entity.projectRoleId())
                .subjectType(entity.subjectType())
                .subjectId(entity.subjectId())
                .createdAt(entity.createdAt())
                .createdBy(entity.createdBy())
                .build();
    }
}
