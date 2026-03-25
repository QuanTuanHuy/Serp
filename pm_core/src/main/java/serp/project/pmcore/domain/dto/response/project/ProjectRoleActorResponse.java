/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.entity.project.ProjectRoleActorEntity;

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

    public static ProjectRoleActorResponse from(ProjectRoleActorEntity entity) {
        return ProjectRoleActorResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .projectRoleId(entity.getProjectRoleId())
                .subjectType(entity.getSubjectType())
                .subjectId(entity.getSubjectId())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
