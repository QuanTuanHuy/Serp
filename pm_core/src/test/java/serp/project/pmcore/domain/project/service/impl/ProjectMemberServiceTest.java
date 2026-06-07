/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.port.IProjectRoleActorPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private IProjectRoleActorPort projectRoleActorPort;

    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private ProjectMemberService service;

    @BeforeEach
    void setUp() {
        service = new ProjectMemberService(projectRoleActorPort, projectPermissionEvaluationService);
    }

    @Test
    void listAssignableMembersShouldReturnEmptyWhenProjectContextMissing() {
        assertEquals(List.of(), service.listAssignableMembers(null));
        assertEquals(List.of(), service.listAssignableMembers(ProjectEntity.builder().id(100L).build()));
        assertEquals(List.of(), service.listAssignableMembers(ProjectEntity.builder().tenantId(1L).build()));
    }

    @Test
    void listAssignableMembersShouldFilterInvalidDistinctAndSort() {
        ProjectEntity project = ProjectEntity.builder()
                .tenantId(1L)
                .id(100L)
                .leadUserId(999L)
                .permissionSchemeId(55L)
                .build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        when(projectRoleActorPort.getProjectRoleActorsByProjectIdAndSubjectType(
                100L,
                ProjectRoleActorSubjectType.USER.name(),
                1L
        )).thenReturn(List.of(
                actor("200"),
                actor("100"),
                actor("300"),
                actor("100"),
                actor("invalid")
        ));
        when(projectPermissionEvaluationService.hasPermission(
                eq(subject),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ASSIGNABLE_USER)
        )).thenAnswer(invocation -> {
            ProjectPermissionEvaluationContext context = invocation.getArgument(1);
            return context.getUserId() != null && context.getUserId() != 200L;
        });

        List<Long> result = service.listAssignableMembers(project);

        assertEquals(List.of(100L, 300L), result);
        verify(projectRoleActorPort).getProjectRoleActorsByProjectIdAndSubjectType(
                100L,
                ProjectRoleActorSubjectType.USER.name(),
                1L
        );
    }

    private ProjectRoleActorEntity actor(String subjectId) {
        return ProjectRoleActorEntity.builder()
                .subjectId(subjectId)
                .build();
    }
}
