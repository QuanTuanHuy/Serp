/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.people;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.people.remove.RemoveProjectPersonCommand;
import serp.project.pmcore.application.project.command.people.remove.RemoveProjectPersonCommandHandler;
import serp.project.pmcore.application.project.command.people.replace.ReplaceProjectPersonRolesCommand;
import serp.project.pmcore.application.project.command.people.replace.ReplaceProjectPersonRolesCommandHandler;
import serp.project.pmcore.application.project.query.people.list.ListProjectPeopleQuery;
import serp.project.pmcore.application.project.query.people.list.ListProjectPeopleQueryHandler;
import serp.project.pmcore.application.project.query.people.list.ProjectPeopleView;
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
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.user.service.IUserService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPeopleHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ACTOR_USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long DEVELOPER_ROLE_ID = 20L;
    private static final Long TESTER_ROLE_ID = 21L;
    private static final Long ADMIN_ROLE_ID = 22L;
    private static final Long PERSON_USER_ID = 99L;
    private static final Long LEAD_USER_ID = 55L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IUserService userService;

    private ListProjectPeopleQueryHandler listHandler;
    private ReplaceProjectPersonRolesCommandHandler replaceHandler;
    private RemoveProjectPersonCommandHandler removeHandler;

    @BeforeEach
    void setUp() {
        listHandler = new ListProjectPeopleQueryHandler(
                projectService,
                projectRoleActorService,
                projectRoleService,
                projectPermissionEvaluationService,
                userService
        );
        replaceHandler = new ReplaceProjectPersonRolesCommandHandler(
                projectService,
                projectRoleService,
                projectRoleActorService,
                projectPermissionEvaluationService,
                userService
        );
        removeHandler = new RemoveProjectPersonCommandHandler(
                projectService,
                projectRoleActorService,
                projectPermissionEvaluationService
        );
    }

    @Test
    void listPeopleShouldGroupUserActorsAndIncludeLead() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectRoleActorService.getActorsByProject(PROJECT_ID, TENANT_ID)).thenReturn(List.of(
                actor(DEVELOPER_ROLE_ID, PERSON_USER_ID, 2_000L),
                actor(TESTER_ROLE_ID, PERSON_USER_ID, 1_000L)
        ));
        when(projectRoleService.getProjectRoleByIdIncludingSystem(DEVELOPER_ROLE_ID, TENANT_ID))
                .thenReturn(role(DEVELOPER_ROLE_ID, "Developer", false));
        when(projectRoleService.getProjectRoleByIdIncludingSystem(TESTER_ROLE_ID, TENANT_ID))
                .thenReturn(role(TESTER_ROLE_ID, "Tester", false));
        when(userService.getUserProfilesByIds(List.of(PERSON_USER_ID, LEAD_USER_ID))).thenReturn(List.of(
                user(PERSON_USER_ID, "Jane", "Member", "jane@example.com"),
                user(LEAD_USER_ID, "Leo", "Lead", "lead@example.com")
        ));

        List<ProjectPeopleView> result = listHandler.handle(new ListProjectPeopleQuery(
                PROJECT_ID,
                TENANT_ID,
                ACTOR_USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        assertEquals(2, result.size());
        ProjectPeopleView member = result.stream()
                .filter(person -> PERSON_USER_ID.equals(person.userId()))
                .findFirst()
                .orElseThrow();
        assertEquals("Jane Member", member.name());
        assertEquals("jane@example.com", member.email());
        assertEquals(2, member.roles().size());
        assertEquals(1_000L, member.addedAt());

        ProjectPeopleView lead = result.stream()
                .filter(person -> LEAD_USER_ID.equals(person.userId()))
                .findFirst()
                .orElseThrow();
        assertTrue(lead.projectLead());
        assertEquals("Leo Lead", lead.name());
        assertTrue(lead.roles().isEmpty());
        assertNull(lead.addedAt());
    }

    @Test
    void replaceRolesShouldRemoveExistingUserActorsAndAssignRequestedRoles() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(userService.getUserById(PERSON_USER_ID)).thenReturn(user(PERSON_USER_ID, "Jane", "Member", "jane@example.com"));
        when(projectRoleService.getProjectRoleByIdIncludingSystem(TESTER_ROLE_ID, TENANT_ID))
                .thenReturn(role(TESTER_ROLE_ID, "Tester", false));
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ADMIN_ROLE_ID, TENANT_ID))
                .thenReturn(role(ADMIN_ROLE_ID, "Administrators", true));

        Unit result = replaceHandler.handle(new ReplaceProjectPersonRolesCommand(
                PROJECT_ID,
                PERSON_USER_ID,
                List.of(TESTER_ROLE_ID, ADMIN_ROLE_ID),
                TENANT_ID,
                ACTOR_USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(projectRoleActorService).removeUserActorsByProject(
                TENANT_ID,
                PROJECT_ID,
                String.valueOf(PERSON_USER_ID),
                ACTOR_USER_ID
        );
        verify(projectRoleActorService).assignActorIfAbsent(
                TENANT_ID,
                PROJECT_ID,
                TESTER_ROLE_ID,
                "USER",
                String.valueOf(PERSON_USER_ID),
                ACTOR_USER_ID
        );
        verify(projectRoleActorService).assignActorIfAbsent(
                TENANT_ID,
                PROJECT_ID,
                ADMIN_ROLE_ID,
                "USER",
                String.valueOf(PERSON_USER_ID),
                ACTOR_USER_ID
        );
        assertSame(Unit.VALUE, result);
    }

    @Test
    void replaceRolesShouldRejectEmptyRoleIds() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> replaceHandler.handle(new ReplaceProjectPersonRolesCommand(
                        PROJECT_ID,
                        PERSON_USER_ID,
                        List.of(),
                        TENANT_ID,
                        ACTOR_USER_ID,
                        Set.of("admins")
                ))
        );

        assertEquals(DomainErrorCode.ROLE_ACTOR_SUBJECT_INVALID, exception.getErrorCode());
    }

    @Test
    void removePersonShouldRemoveAllUserActors() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);

        Unit result = removeHandler.handle(new RemoveProjectPersonCommand(
                PROJECT_ID,
                PERSON_USER_ID,
                TENANT_ID,
                ACTOR_USER_ID,
                Set.of("admins")
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        assertEquals(ACTOR_USER_ID, contextCaptor.getValue().getUserId());
        verify(projectRoleActorService).removeUserActorsByProject(
                TENANT_ID,
                PROJECT_ID,
                String.valueOf(PERSON_USER_ID),
                ACTOR_USER_ID
        );
        assertSame(Unit.VALUE, result);
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .leadUserId(LEAD_USER_ID)
                .build();
    }

    private ProjectRoleActorEntity actor(Long roleId, Long userId, Long createdAt) {
        return ProjectRoleActorEntity.builder()
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .projectRoleId(roleId)
                .subjectType("USER")
                .subjectId(String.valueOf(userId))
                .createdAt(createdAt)
                .createdBy(ACTOR_USER_ID)
                .updatedAt(createdAt)
                .updatedBy(ACTOR_USER_ID)
                .build();
    }

    private ProjectRoleEntity role(Long id, String name, boolean system) {
        return ProjectRoleEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .name(name)
                .isSystem(system)
                .build();
    }

    private UserProfileDto user(Long id, String firstName, String lastName, String email) {
        return UserProfileDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .avatarUrl("https://cdn.example.com/" + id + ".png")
                .build();
    }
}
