/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.roleactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.roleactor.RoleActorEventPayload;
import serp.project.pmcore.application.project.command.roleactor.RoleActorOutboxPublisher;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorCommand;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorCommandHandler;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorResult;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorValidator;
import serp.project.pmcore.application.project.command.roleactor.remove.RemoveProjectRoleActorCommand;
import serp.project.pmcore.application.project.command.roleactor.remove.RemoveProjectRoleActorCommandHandler;
import serp.project.pmcore.application.project.command.roleactor.remove.RemoveProjectRoleActorValidator;
import serp.project.pmcore.application.project.query.roleactor.list.ListProjectRoleActorsQuery;
import serp.project.pmcore.application.project.query.roleactor.list.ListProjectRoleActorsQueryHandler;
import serp.project.pmcore.application.project.query.roleactor.list.ProjectRoleActorView;
import serp.project.pmcore.application.shared.cqrs.Unit;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectRoleActorHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROLE_ID = 20L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private AddProjectRoleActorValidator addProjectRoleActorValidator;
    @Mock
    private RemoveProjectRoleActorValidator removeProjectRoleActorValidator;
    @Mock
    private RoleActorOutboxPublisher roleActorOutboxPublisher;

    private AddProjectRoleActorCommandHandler addHandler;
    private RemoveProjectRoleActorCommandHandler removeHandler;
    private ListProjectRoleActorsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        addHandler = new AddProjectRoleActorCommandHandler(
                projectService,
                projectRoleService,
                projectRoleActorService,
                projectPermissionEvaluationService,
                addProjectRoleActorValidator,
                roleActorOutboxPublisher
        );
        removeHandler = new RemoveProjectRoleActorCommandHandler(
                projectService,
                projectRoleService,
                projectRoleActorService,
                projectPermissionEvaluationService,
                removeProjectRoleActorValidator,
                roleActorOutboxPublisher
        );
        listHandler = new ListProjectRoleActorsQueryHandler(
                projectService,
                projectRoleService,
                projectRoleActorService,
                projectPermissionEvaluationService
        );
    }

    @Test
    void addHandlerShouldAssignActorAndReturnResult() {
        ProjectEntity project = project();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ROLE_ID, TENANT_ID))
                .thenReturn(ProjectRoleEntity.builder().id(ROLE_ID).build());
        when(addProjectRoleActorValidator.validate(any(AddProjectRoleActorCommand.class)))
                .thenReturn(new AddProjectRoleActorValidator.ValidatedRoleActorSubject("USER", "99"));
        when(projectRoleActorService.assignActor(
                TENANT_ID,
                PROJECT_ID,
                ROLE_ID,
                "USER",
                "99",
                USER_ID
        )).thenReturn(actorEntity(100L, "USER", "99"));

        AddProjectRoleActorResult result = addHandler.handle(new AddProjectRoleActorCommand(
                PROJECT_ID,
                ROLE_ID,
                "user",
                "99",
                TENANT_ID,
                USER_ID,
                Set.of("dev-team")
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );

        ProjectPermissionEvaluationContext context = contextCaptor.getValue();
        assertEquals(USER_ID, context.getUserId());
        assertEquals(Set.of("dev-team"), context.getGroupKeys());
        assertEquals(100L, result.id());
        assertEquals(PROJECT_ID, result.projectId());
        assertEquals("USER", result.subjectType());
        assertEquals("99", result.subjectId());
        assertEquals(USER_ID, result.updatedBy());

        ArgumentCaptor<RoleActorEventPayload> payloadCaptor = ArgumentCaptor.forClass(RoleActorEventPayload.class);
        verify(roleActorOutboxPublisher).publishRoleActorAdded(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(PROJECT_ID, payloadCaptor.getValue().projectId());
        assertEquals(ROLE_ID, payloadCaptor.getValue().roleId());
        assertEquals("USER", payloadCaptor.getValue().subjectType());
        assertEquals("99", payloadCaptor.getValue().subjectId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());
    }

    @Test
    void removeHandlerShouldRemoveAssignedActor() {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project());
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ROLE_ID, TENANT_ID))
                .thenReturn(ProjectRoleEntity.builder().id(ROLE_ID).build());
        when(removeProjectRoleActorValidator.validate(any(RemoveProjectRoleActorCommand.class)))
                .thenReturn(new RemoveProjectRoleActorValidator.ValidatedRoleActorSubject("GROUP", "ops-team"));

        Unit result = removeHandler.handle(new RemoveProjectRoleActorCommand(
                PROJECT_ID,
                ROLE_ID,
                "group",
                "ops-team",
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        verify(projectRoleActorService).removeActor(
                TENANT_ID,
                PROJECT_ID,
                ROLE_ID,
                "GROUP",
                "ops-team",
                USER_ID
        );
        verify(roleActorOutboxPublisher).publishRoleActorRemoved(eq(TENANT_ID), any(RoleActorEventPayload.class));
        assertSame(Unit.VALUE, result);
    }

    @Test
    void listHandlerShouldReturnMappedViews() {
        ProjectEntity project = project();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ROLE_ID, TENANT_ID))
                .thenReturn(ProjectRoleEntity.builder().id(ROLE_ID).build());
        when(projectRoleActorService.getActorsByProjectAndRole(PROJECT_ID, ROLE_ID, TENANT_ID))
                .thenReturn(List.of(
                        actorEntity(101L, "USER", "99"),
                        actorEntity(102L, "GROUP", "ops-team")
                ));

        List<ProjectRoleActorView> result = listHandler.handle(new ListProjectRoleActorsQuery(
                PROJECT_ID,
                ROLE_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).id());
        assertEquals("ops-team", result.get(1).subjectId());
        assertEquals(USER_ID, result.get(0).updatedBy());
    }

    @Test
    void addHandlerShouldRejectWhenProjectArchived() {
        ProjectEntity archivedProject = project();
        archivedProject.setIsArchived(true);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(archivedProject);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> addHandler.handle(new AddProjectRoleActorCommand(
                        PROJECT_ID,
                        ROLE_ID,
                        "user",
                        "99",
                        TENANT_ID,
                        USER_ID,
                        Set.of("dev-team")
                ))
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, exception.getErrorCode());
    }

    @Test
    void removeHandlerShouldRejectWhenProjectArchived() {
        ProjectEntity archivedProject = project();
        archivedProject.setIsArchived(true);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(archivedProject);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> removeHandler.handle(new RemoveProjectRoleActorCommand(
                        PROJECT_ID,
                        ROLE_ID,
                        "group",
                        "ops-team",
                        TENANT_ID,
                        USER_ID,
                        Set.of("admins")
                ))
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, exception.getErrorCode());
    }

    @Test
    void listHandlerShouldAllowArchivedProject() {
        ProjectEntity archivedProject = project();
        archivedProject.setIsArchived(true);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(archivedProject);
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ROLE_ID, TENANT_ID))
                .thenReturn(ProjectRoleEntity.builder().id(ROLE_ID).build());
        when(projectRoleActorService.getActorsByProjectAndRole(PROJECT_ID, ROLE_ID, TENANT_ID))
                .thenReturn(List.of(actorEntity(101L, "USER", "99")));

        List<ProjectRoleActorView> result = listHandler.handle(new ListProjectRoleActorsQuery(
                PROJECT_ID,
                ROLE_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(archivedProject)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        assertEquals(1, result.size());
        assertEquals(101L, result.getFirst().id());
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build();
    }

    private ProjectRoleActorEntity actorEntity(Long id, String subjectType, String subjectId) {
        return ProjectRoleActorEntity.builder()
                .id(id)
                .projectId(PROJECT_ID)
                .projectRoleId(ROLE_ID)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .createdAt(1_700_000_000_000L)
                .createdBy(USER_ID)
                .updatedAt(1_700_000_000_000L)
                .updatedBy(USER_ID)
                .build();
    }
}
