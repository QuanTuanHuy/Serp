/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectSettingsOverviewQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long CATEGORY_ID = 11L;
    private static final Long LEAD_USER_ID = 55L;
    private static final Long ISSUE_TYPE_SCHEME_ID = 101L;
    private static final Long WORKFLOW_SCHEME_ID = 102L;
    private static final Long PRIORITY_SCHEME_ID = 103L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectComponentService projectComponentService;
    @Mock
    private IProjectCategoryPort projectCategoryPort;
    @Mock
    private IIssueTypeSchemeService issueTypeSchemeService;
    @Mock
    private IWorkflowSchemeService workflowSchemeService;
    @Mock
    private IPrioritySchemeService prioritySchemeService;
    @Mock
    private IUserService userService;

    private GetProjectSettingsOverviewQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetProjectSettingsOverviewQueryHandler(
                projectService,
                projectPermissionEvaluationService,
                projectRoleActorService,
                projectRoleService,
                projectComponentService,
                projectCategoryPort,
                issueTypeSchemeService,
                workflowSchemeService,
                prioritySchemeService,
                userService
        );
    }

    @Test
    void handleShouldReturnCoreConfigurationOverview() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectCategoryPort.getCategoryByIdIncludingSystem(CATEGORY_ID, TENANT_ID))
                .thenReturn(Optional.of(category()));
        when(userService.getUserProfilesByIds(List.of(LEAD_USER_ID))).thenReturn(List.of(leadUser()));
        when(projectRoleActorService.getActorsByProject(PROJECT_ID, TENANT_ID)).thenReturn(List.of(
                roleActor(201L, "99"),
                roleActor(202L, "99"),
                roleActor(201L, "100")
        ));
        when(projectRoleService.getProjectRoleByIdIncludingSystem(201L, TENANT_ID)).thenReturn(null);
        when(projectRoleService.getProjectRoleByIdIncludingSystem(202L, TENANT_ID)).thenReturn(null);
        when(projectComponentService.listComponents(eq(PROJECT_ID), eq(TENANT_ID), any(ProjectComponentListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(component(301L, "Backend")), 7L));
        when(issueTypeSchemeService.getVisibleIssueTypeSchemeById(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenReturn(issueTypeScheme());
        when(workflowSchemeService.getVisibleWorkflowSchemeById(WORKFLOW_SCHEME_ID, TENANT_ID))
                .thenReturn(workflowScheme());
        when(prioritySchemeService.getVisiblePrioritySchemeById(PRIORITY_SCHEME_ID, TENANT_ID))
                .thenReturn(priorityScheme());

        ProjectSettingsOverviewView result = handler.handle(new GetProjectSettingsOverviewQuery(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals("SERP", result.project().key());
        assertEquals("SERP Platform", result.project().name());
        assertEquals("Engineering", result.project().category().name());
        assertEquals("Leo Lead", result.project().leadUserName());
        assertEquals(3L, result.people().memberCount());
        assertEquals(2L, result.people().roleCount());
        assertEquals(7L, result.components().totalCount());
        assertEquals("Backend", result.components().preview().getFirst().name());
        assertEquals(8, result.schemes().size());
        assertEquals("Work type scheme", result.schemes().getFirst().label());
        assertEquals("Software work types", result.schemes().getFirst().schemeName());
        assertEquals("work-type-schemes", result.schemes().getFirst().globalSection());
        assertTrue(result.schemes().getFirst().available());
    }

    @Test
    void handleShouldKeepUnsupportedSchemesReadOnlyAndUnresolvedNamesNull() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectCategoryPort.getCategoryByIdIncludingSystem(CATEGORY_ID, TENANT_ID))
                .thenReturn(Optional.empty());
        when(userService.getUserProfilesByIds(List.of(LEAD_USER_ID))).thenReturn(List.of());
        when(projectRoleActorService.getActorsByProject(PROJECT_ID, TENANT_ID)).thenReturn(List.of());
        when(projectComponentService.listComponents(eq(PROJECT_ID), eq(TENANT_ID), any(ProjectComponentListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(), 0L));
        when(issueTypeSchemeService.getVisibleIssueTypeSchemeById(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenThrow(ResourceNotFoundException.issueTypeScheme(ISSUE_TYPE_SCHEME_ID));
        when(workflowSchemeService.getVisibleWorkflowSchemeById(WORKFLOW_SCHEME_ID, TENANT_ID))
                .thenThrow(ResourceNotFoundException.workflowScheme(WORKFLOW_SCHEME_ID));
        when(prioritySchemeService.getVisiblePrioritySchemeById(PRIORITY_SCHEME_ID, TENANT_ID))
                .thenThrow(ResourceNotFoundException.priorityScheme(PRIORITY_SCHEME_ID));

        ProjectSettingsOverviewView result = handler.handle(new GetProjectSettingsOverviewQuery(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of()
        ));

        ProjectSettingsOverviewView.SchemeBindingView fieldConfig = result.schemes().stream()
                .filter(scheme -> "FIELD_CONFIG".equals(scheme.type()))
                .findFirst()
                .orElseThrow();
        assertEquals(104L, fieldConfig.schemeId());
        assertNull(fieldConfig.schemeName());
        assertNull(fieldConfig.globalSection());
        assertFalse(fieldConfig.available());

        ProjectSettingsOverviewView.SchemeBindingView issueType = result.schemes().getFirst();
        assertEquals(ISSUE_TYPE_SCHEME_ID, issueType.schemeId());
        assertNull(issueType.schemeName());
        assertEquals("work-type-schemes", issueType.globalSection());
        assertTrue(issueType.available());
        assertEquals("User #55", result.people().leadUserName());
        assertNull(result.project().category());
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .description("Core project")
                .url("https://serp.local")
                .projectTypeKey("software")
                .leadUserId(LEAD_USER_ID)
                .categoryId(CATEGORY_ID)
                .isArchived(false)
                .issueTypeSchemeId(ISSUE_TYPE_SCHEME_ID)
                .workflowSchemeId(WORKFLOW_SCHEME_ID)
                .prioritySchemeId(PRIORITY_SCHEME_ID)
                .fieldConfigSchemeId(104L)
                .issueTypeScreenSchemeId(105L)
                .permissionSchemeId(106L)
                .notificationSchemeId(107L)
                .issueSecuritySchemeId(108L)
                .build();
    }

    private ProjectCategoryEntity category() {
        return ProjectCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(TENANT_ID)
                .name("Engineering")
                .build();
    }

    private UserProfileDto leadUser() {
        return UserProfileDto.builder()
                .id(LEAD_USER_ID)
                .firstName("Leo")
                .lastName("Lead")
                .email("lead@example.com")
                .build();
    }

    private ProjectRoleActorEntity roleActor(Long roleId, String subjectId) {
        return ProjectRoleActorEntity.builder()
                .projectId(PROJECT_ID)
                .tenantId(TENANT_ID)
                .projectRoleId(roleId)
                .subjectType("USER")
                .subjectId(subjectId)
                .build();
    }

    private ProjectComponentEntity component(Long id, String name) {
        return ProjectComponentEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .name(name)
                .description("Backend component")
                .issueCount(5L)
                .assigneeType("PROJECT_DEFAULT")
                .build();
    }

    private IssueTypeSchemeEntity issueTypeScheme() {
        return IssueTypeSchemeEntity.builder().id(ISSUE_TYPE_SCHEME_ID).name("Software work types").build();
    }

    private WorkflowSchemeEntity workflowScheme() {
        return WorkflowSchemeEntity.builder().id(WORKFLOW_SCHEME_ID).name("Default workflow scheme").build();
    }

    private PrioritySchemeEntity priorityScheme() {
        return PrioritySchemeEntity.builder().id(PRIORITY_SCHEME_ID).name("Default priority scheme").build();
    }
}
