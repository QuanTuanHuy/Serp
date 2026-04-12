/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.roleactor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorResult;
import serp.project.pmcore.application.project.query.roleactor.list.ProjectRoleActorView;

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
    private Long updatedAt;
    private Long updatedBy;

    public static ProjectRoleActorResponse from(ProjectRoleActorView entity) {
        return ProjectRoleActorResponse.builder()
                .id(entity.id())
                .projectId(entity.projectId())
                .projectRoleId(entity.projectRoleId())
                .subjectType(entity.subjectType())
                .subjectId(entity.subjectId())
                .createdAt(entity.createdAt())
                .createdBy(entity.createdBy())
                .updatedAt(entity.updatedAt())
                .updatedBy(entity.updatedBy())
                .build();
    }

    public static ProjectRoleActorResponse from(AddProjectRoleActorResult entity) {
        return ProjectRoleActorResponse.builder()
                .id(entity.id())
                .projectId(entity.projectId())
                .projectRoleId(entity.projectRoleId())
                .subjectType(entity.subjectType())
                .subjectId(entity.subjectId())
                .createdAt(entity.createdAt())
                .createdBy(entity.createdBy())
                .updatedAt(entity.updatedAt())
                .updatedBy(entity.updatedBy())
                .build();
    }
}
