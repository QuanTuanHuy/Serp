# PM Project Settings Overview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a backend-composed PM project settings overview endpoint and use it to render the project settings tab as a launcher.

**Architecture:** Add a focused Spring query handler that composes project details, people summary, component preview, and scheme bindings behind `GET /api/v1/projects/{projectId}/settings-overview`. Add one RTK Query endpoint and update `PMProjectSettingsPage` to render from the overview contract while keeping existing archive/unarchive mutations.

**Tech Stack:** Java 21, Spring Boot 3.5, JUnit 5, Mockito, Next.js 15, React 19, TypeScript, RTK Query, Tailwind/Shadcn UI.

---

## File Structure

Backend files:

- Create `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQuery.java`
  - Query input record: project id, tenant id, actor user id, actor group keys.
- Create `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/ProjectSettingsOverviewView.java`
  - API-facing view with nested records for project, people, components, and schemes.
- Create `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandler.java`
  - Composes the overview and checks `BROWSE_PROJECTS`.
- Modify `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/ProjectController.java`
  - Add `GET /{id}/settings-overview`.
- Test `pm_core/src/test/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandlerTest.java`
  - Focused query handler tests.

Frontend files:

- Modify `serp_web/src/modules/pm/types/project-api.types.ts`
  - Add `PMProjectSettingsOverviewApi`.
- Modify `serp_web/src/modules/pm/api/projectApi.ts`
  - Add `getPmProjectSettingsOverview` query and export hook.
- Modify `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`
  - Render overview launcher from the new endpoint.

---

### Task 1: Backend Query Contract And Failing Tests

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQuery.java`
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/ProjectSettingsOverviewView.java`
- Create: `pm_core/src/test/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandlerTest.java`

- [ ] **Step 1: Add query input record**

Create `GetProjectSettingsOverviewQuery.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import java.util.Set;

public record GetProjectSettingsOverviewQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) {
}
```

- [ ] **Step 2: Add overview view contract**

Create `ProjectSettingsOverviewView.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import java.util.List;

public record ProjectSettingsOverviewView(
        ProjectView project,
        PeopleView people,
        ComponentsView components,
        List<SchemeBindingView> schemes
) {
    public record ProjectView(
            Long id,
            String key,
            String name,
            String description,
            String url,
            String projectTypeKey,
            Boolean isArchived,
            Long archivedAt,
            Long leadUserId,
            String leadUserName,
            CategoryView category
    ) {
    }

    public record CategoryView(Long id, String name) {
    }

    public record PeopleView(
            Long leadUserId,
            String leadUserName,
            long memberCount,
            long roleCount
    ) {
    }

    public record ComponentsView(
            long totalCount,
            List<ComponentPreviewView> preview
    ) {
    }

    public record ComponentPreviewView(
            Long id,
            String name,
            String description,
            Long issueCount,
            String assigneeType
    ) {
    }

    public record SchemeBindingView(
            String type,
            String label,
            Long schemeId,
            String schemeName,
            String globalSection,
            boolean available
    ) {
    }
}
```

- [ ] **Step 3: Write failing handler test**

Create `GetProjectSettingsOverviewQueryHandlerTest.java`:

```java
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
                .isArchived(false)
                .category(ProjectCategoryEntity.builder().id(11L).name("Engineering").build())
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
```

- [ ] **Step 4: Run the focused test and verify it fails**

Run from `pm_core`:

```bash
./mvnw.cmd -Dtest=GetProjectSettingsOverviewQueryHandlerTest test
```

Expected: compilation fails because `GetProjectSettingsOverviewQueryHandler` does not exist yet.

- [ ] **Step 5: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQuery.java pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/ProjectSettingsOverviewView.java pm_core/src/test/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandlerTest.java
git commit -m "test(pm): cover project settings overview query"
```

---

### Task 2: Backend Query Handler

**Files:**
- Create: `pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandler.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandlerTest.java`

- [ ] **Step 1: Implement the handler**

Create `GetProjectSettingsOverviewQueryHandler.java`:

```java
/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class GetProjectSettingsOverviewQueryHandler
        implements IQueryHandler<GetProjectSettingsOverviewQuery, ProjectSettingsOverviewView> {

    private static final String USER_SUBJECT_TYPE = "USER";

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectRoleService projectRoleService;
    private final IProjectComponentService projectComponentService;
    private final IIssueTypeSchemeService issueTypeSchemeService;
    private final IWorkflowSchemeService workflowSchemeService;
    private final IPrioritySchemeService prioritySchemeService;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public ProjectSettingsOverviewView handle(GetProjectSettingsOverviewQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(query.userId())
                        .groupKeys(query.groupKeys())
                        .build(),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        String leadUserName = resolveLeadUserName(project.getLeadUserId());
        PeopleSummary peopleSummary = buildPeopleSummary(project, query.tenantId(), leadUserName);
        PageResult<ProjectComponentEntity> components = projectComponentService.listComponents(
                query.projectId(),
                query.tenantId(),
                ProjectComponentListCriteria.builder()
                        .page(0)
                        .pageSize(5)
                        .sortBy("name")
                        .sortDirection("ASC")
                        .build()
        );

        return new ProjectSettingsOverviewView(
                toProjectView(project, leadUserName),
                new ProjectSettingsOverviewView.PeopleView(
                        project.getLeadUserId(),
                        leadUserName,
                        peopleSummary.memberCount(),
                        peopleSummary.roleCount()
                ),
                new ProjectSettingsOverviewView.ComponentsView(
                        components.total(),
                        components.items().stream().map(this::toComponentPreview).toList()
                ),
                buildSchemes(project, query.tenantId())
        );
    }

    private ProjectSettingsOverviewView.ProjectView toProjectView(ProjectEntity project, String leadUserName) {
        ProjectSettingsOverviewView.CategoryView category = project.getCategory() == null
                ? null
                : new ProjectSettingsOverviewView.CategoryView(
                        project.getCategory().getId(),
                        project.getCategory().getName()
                );
        return new ProjectSettingsOverviewView.ProjectView(
                project.getId(),
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.getUrl(),
                project.getProjectTypeKey(),
                project.getIsArchived(),
                project.getArchivedAt(),
                project.getLeadUserId(),
                leadUserName,
                category
        );
    }

    private PeopleSummary buildPeopleSummary(ProjectEntity project, Long tenantId, String leadUserName) {
        List<ProjectRoleActorEntity> userActors = projectRoleActorService.getActorsByProject(project.getId(), tenantId)
                .stream()
                .filter(actor -> USER_SUBJECT_TYPE.equalsIgnoreCase(actor.getSubjectType()))
                .filter(actor -> parseUserId(actor.getSubjectId()) != null)
                .toList();
        Set<Long> memberUserIds = new LinkedHashSet<>();
        for (ProjectRoleActorEntity actor : userActors) {
            memberUserIds.add(parseUserId(actor.getSubjectId()));
        }
        if (project.getLeadUserId() != null) {
            memberUserIds.add(project.getLeadUserId());
        }
        long roleCount = userActors.stream()
                .map(ProjectRoleActorEntity::getProjectRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .filter(roleId -> projectRoleService.getProjectRoleByIdIncludingSystem(roleId, tenantId) != null)
                .count();
        return new PeopleSummary(memberUserIds.size(), roleCount);
    }

    private ProjectSettingsOverviewView.ComponentPreviewView toComponentPreview(ProjectComponentEntity component) {
        return new ProjectSettingsOverviewView.ComponentPreviewView(
                component.getId(),
                component.getName(),
                component.getDescription(),
                component.getIssueCount() == null ? 0L : component.getIssueCount(),
                component.getAssigneeType()
        );
    }

    private List<ProjectSettingsOverviewView.SchemeBindingView> buildSchemes(ProjectEntity project, Long tenantId) {
        return List.of(
                scheme("ISSUE_TYPE", "Work type scheme", project.getIssueTypeSchemeId(), "work-type-schemes", true,
                        () -> resolveIssueTypeSchemeName(project.getIssueTypeSchemeId(), tenantId)),
                scheme("WORKFLOW", "Workflow scheme", project.getWorkflowSchemeId(), "workflow-schemes", true,
                        () -> resolveWorkflowSchemeName(project.getWorkflowSchemeId(), tenantId)),
                scheme("FIELD_CONFIG", "Field config scheme", project.getFieldConfigSchemeId(), null, false, () -> null),
                scheme("SCREEN", "Screen scheme", project.getIssueTypeScreenSchemeId(), null, false, () -> null),
                scheme("PERMISSION", "Permission scheme", project.getPermissionSchemeId(), null, false, () -> null),
                scheme("NOTIFICATION", "Notification scheme", project.getNotificationSchemeId(), null, false, () -> null),
                scheme("PRIORITY", "Priority scheme", project.getPrioritySchemeId(), "priority-schemes", true,
                        () -> resolvePrioritySchemeName(project.getPrioritySchemeId(), tenantId)),
                scheme("ISSUE_SECURITY", "Issue security scheme", project.getIssueSecuritySchemeId(), null, false, () -> null)
        );
    }

    private ProjectSettingsOverviewView.SchemeBindingView scheme(String type,
                                                                  String label,
                                                                  Long schemeId,
                                                                  String globalSection,
                                                                  boolean available,
                                                                  Supplier<String> nameSupplier) {
        String schemeName = null;
        if (schemeId != null) {
            try {
                schemeName = nameSupplier.get();
            } catch (RuntimeException ignored) {
                schemeName = null;
            }
        }
        return new ProjectSettingsOverviewView.SchemeBindingView(
                type,
                label,
                schemeId,
                schemeName,
                globalSection,
                available
        );
    }

    private String resolveIssueTypeSchemeName(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return null;
        }
        IssueTypeSchemeEntity scheme = issueTypeSchemeService.getVisibleIssueTypeSchemeById(schemeId, tenantId);
        return scheme.getName();
    }

    private String resolveWorkflowSchemeName(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return null;
        }
        WorkflowSchemeEntity scheme = workflowSchemeService.getVisibleWorkflowSchemeById(schemeId, tenantId);
        return scheme.getName();
    }

    private String resolvePrioritySchemeName(Long schemeId, Long tenantId) {
        if (schemeId == null) {
            return null;
        }
        PrioritySchemeEntity scheme = prioritySchemeService.getVisiblePrioritySchemeById(schemeId, tenantId);
        return scheme.getName();
    }

    private String resolveLeadUserName(Long leadUserId) {
        if (leadUserId == null) {
            return null;
        }
        return userService.getUserProfilesByIds(List.of(leadUserId))
                .stream()
                .findFirst()
                .map(this::displayName)
                .orElse("User #" + leadUserId);
    }

    private String displayName(UserProfileDto profile) {
        String fullName = profile.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return profile.getEmail() == null || profile.getEmail().isBlank()
                ? "User #" + profile.getId()
                : profile.getEmail();
    }

    private Long parseUserId(String subjectId) {
        try {
            return Long.parseLong(subjectId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record PeopleSummary(long memberCount, long roleCount) {
    }
}
```

- [ ] **Step 2: Run focused handler tests**

Run from `pm_core`:

```bash
./mvnw.cmd -Dtest=GetProjectSettingsOverviewQueryHandlerTest test
```

Expected: tests pass.

- [ ] **Step 3: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandler.java
git commit -m "feat(pm): compose project settings overview"
```

---

### Task 3: Backend REST Endpoint

**Files:**
- Modify: `pm_core/src/main/java/serp/project/pmcore/ui/rest/project/ProjectController.java`
- Test: `pm_core/src/test/java/serp/project/pmcore/application/project/query/settings/GetProjectSettingsOverviewQueryHandlerTest.java`

- [ ] **Step 1: Add controller imports**

Add these imports to `ProjectController.java`:

```java
import serp.project.pmcore.application.project.query.settings.GetProjectSettingsOverviewQuery;
import serp.project.pmcore.application.project.query.settings.GetProjectSettingsOverviewQueryHandler;
import serp.project.pmcore.application.project.query.settings.ProjectSettingsOverviewView;
```

- [ ] **Step 2: Inject the new handler**

Add this field near the other query handlers:

```java
private final GetProjectSettingsOverviewQueryHandler getProjectSettingsOverviewQueryHandler;
```

- [ ] **Step 3: Add REST method**

Add this method after `getProjectById`:

```java
@GetMapping("/{id}/settings-overview")
public ResponseEntity<GeneralResponse<ProjectSettingsOverviewView>> getProjectSettingsOverview(
        @PathVariable("id") Long projectId) {
    Long userId = authUtils.getCurrentUserId()
            .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
    Long tenantId = authUtils.getCurrentTenantId()
            .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

    ProjectSettingsOverviewView response = getProjectSettingsOverviewQueryHandler.handle(
            new GetProjectSettingsOverviewQuery(
                    projectId,
                    tenantId,
                    userId,
                    authUtils.getCurrentGroups()
            )
    );
    return ResponseEntity.ok(responseUtils.success(response));
}
```

- [ ] **Step 4: Run backend compile**

Run from `pm_core`:

```bash
./mvnw.cmd clean compile
```

Expected: compile succeeds.

- [ ] **Step 5: Run focused handler test again**

Run from `pm_core`:

```bash
./mvnw.cmd -Dtest=GetProjectSettingsOverviewQueryHandlerTest test
```

Expected: tests pass.

- [ ] **Step 6: Commit**

```bash
git add pm_core/src/main/java/serp/project/pmcore/ui/rest/project/ProjectController.java
git commit -m "feat(pm): expose project settings overview endpoint"
```

---

### Task 4: Frontend API Contract

**Files:**
- Modify: `serp_web/src/modules/pm/types/project-api.types.ts`
- Modify: `serp_web/src/modules/pm/api/projectApi.ts`

- [ ] **Step 1: Add TypeScript overview types**

Append to `project-api.types.ts`:

```ts
export type PMProjectSettingsSchemeType =
  | 'ISSUE_TYPE'
  | 'WORKFLOW'
  | 'FIELD_CONFIG'
  | 'SCREEN'
  | 'PERMISSION'
  | 'NOTIFICATION'
  | 'PRIORITY'
  | 'ISSUE_SECURITY';

export interface PMProjectSettingsOverviewApi {
  project: {
    id: number;
    key: string;
    name: string;
    description?: string | null;
    url?: string | null;
    projectTypeKey: string;
    isArchived: boolean;
    archivedAt?: number | string | null;
    leadUserId?: number | null;
    leadUserName?: string | null;
    category?: {
      id: number;
      name: string;
    } | null;
  };
  people: {
    leadUserId?: number | null;
    leadUserName?: string | null;
    memberCount: number;
    roleCount: number;
  };
  components: {
    totalCount: number;
    preview: Array<{
      id: number;
      name: string;
      description?: string | null;
      issueCount: number;
      assigneeType: string;
    }>;
  };
  schemes: Array<{
    type: PMProjectSettingsSchemeType;
    label: string;
    schemeId?: number | null;
    schemeName?: string | null;
    globalSection?: string | null;
    available: boolean;
  }>;
}
```

- [ ] **Step 2: Import the type in `projectApi.ts`**

Add `PMProjectSettingsOverviewApi` to the existing type import from `../types/api`:

```ts
import type {
  PMCreateProjectRequest,
  PMCreateProjectResponse,
  PMCreateProjectComponentRequest,
  PMDeleteProjectComponentResponse,
  PMListProjectsParams,
  PMListProjectComponentsParams,
  PMProjectPersonApi,
  PMProjectRoleApi,
  PMProjectBlueprintApi,
  PMProjectCategoryApi,
  PMProjectComponentApi,
  PMProjectSettingsOverviewApi,
  PMProjectSummaryDashboardApi,
  PMProjectSummaryApi,
  PMProjectDetailApi,
  PMReplaceProjectPersonRolesRequest,
  PMProjectSummaryFilterParams,
  PMUpdateProjectComponentRequest,
  PMUpdateProjectRequest,
  PMUpdateProjectResponse,
} from '../types/api';
```

- [ ] **Step 3: Add RTK Query endpoint**

Add this endpoint after `getPmProjectById`:

```ts
getPmProjectSettingsOverview: builder.query<
  PMProjectSettingsOverviewApi,
  string
>({
  query: (id) => ({
    url: `/projects/${id}/settings-overview`,
    method: 'GET',
  }),
  extraOptions: { service: 'pm' },
  transformResponse: createDataTransform<PMProjectSettingsOverviewApi>(),
  providesTags: (_result, _error, id) => [
    { type: 'pm/ProjectSettingsOverview' as const, id },
  ],
}),
```

- [ ] **Step 4: Invalidate overview after archive and unarchive**

Update `archivePmProject.invalidatesTags`:

```ts
invalidatesTags: (result, error, id) => [
  { type: 'pm/Project' as const, id },
  { type: 'pm/Project' as const, id: 'LIST' },
  { type: 'pm/ProjectSettingsOverview' as const, id },
],
```

Update `unarchivePmProject.invalidatesTags`:

```ts
invalidatesTags: (result, error, id) => [
  { type: 'pm/Project' as const, id },
  { type: 'pm/Project' as const, id: 'LIST' },
  { type: 'pm/ProjectSettingsOverview' as const, id },
],
```

- [ ] **Step 5: Export the generated hook**

Add the hook to the export block:

```ts
useGetPmProjectSettingsOverviewQuery,
```

- [ ] **Step 6: Run frontend type-check**

Run from `serp_web`:

```bash
npm run type-check
```

Expected: type-check passes.

- [ ] **Step 7: Commit**

```bash
git add serp_web/src/modules/pm/types/project-api.types.ts serp_web/src/modules/pm/api/projectApi.ts
git commit -m "feat(web): add project settings overview api"
```

---

### Task 5: Frontend Project Settings Launcher Page

**Files:**
- Modify: `serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx`

- [ ] **Step 1: Replace data source imports**

Replace the old project detail/components query imports with the overview hook. Keep archive hooks:

```ts
import {
  useArchivePmProjectMutation,
  useGetPmProjectSettingsOverviewQuery,
  useUnarchivePmProjectMutation,
} from '../api';
```

- [ ] **Step 2: Use overview query in the component**

Replace `useGetPmProjectByIdQuery` and `useGetPmProjectComponentsQuery` with:

```ts
const {
  data: overview,
  isLoading: isOverviewLoading,
  error: overviewError,
} = useGetPmProjectSettingsOverviewQuery(projectId);
```

- [ ] **Step 3: Update archive toggle**

Use `overview.project`:

```ts
const handleArchiveToggle = async () => {
  if (!overview?.project) {
    return;
  }

  try {
    if (overview.project.isArchived) {
      await unarchivePmProject(projectId).unwrap();
      toast.success('Project unarchived.');
    } else {
      await archivePmProject(projectId).unwrap();
      toast.success('Project archived.');
    }
  } catch (error) {
    toast.error('Unable to update project state', {
      description: getErrorMessage(error),
    });
  }
};
```

- [ ] **Step 4: Replace loading and unavailable states**

Use these states:

```tsx
if (isOverviewLoading) {
  return (
    <div className='rounded-lg border bg-card p-6 text-sm text-muted-foreground shadow-sm'>
      Loading project settings...
    </div>
  );
}

if (overviewError || !overview) {
  return (
    <Card className='shadow-sm'>
      <CardContent className='space-y-4 p-6'>
        <p className='text-sm text-muted-foreground'>
          Project settings unavailable.
        </p>
        <Button
          type='button'
          variant='outline'
          onClick={() => router.push('/pm/projects')}
        >
          <ArrowLeft className='mr-2 h-4 w-4' />
          Back to projects
        </Button>
      </CardContent>
    </Card>
  );
}
```

- [ ] **Step 5: Add local constants after unavailable state**

```ts
const { project, people, components, schemes } = overview;
const componentPreview = components.preview;
```

- [ ] **Step 6: Render profile, people, components, and scheme launcher**

Replace the existing return body with this structure:

```tsx
return (
  <div className='space-y-6'>
    <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
      <div className='space-y-3'>
        <Button
          type='button'
          variant='ghost'
          className='w-fit px-0 text-muted-foreground hover:bg-transparent'
          onClick={() => router.push(`/pm/projects/${projectId}/summary`)}
        >
          <ArrowLeft className='mr-2 h-4 w-4' />
          Back to project
        </Button>

        <div className='flex items-center gap-3'>
          <div className='flex h-11 w-11 items-center justify-center rounded-md bg-primary/10 text-primary'>
            <PenLine className='h-5 w-5' />
          </div>
          <div>
            <h1 className='text-3xl font-bold tracking-tight'>
              Project settings
            </h1>
            <p className='text-sm text-muted-foreground'>
              {project.name} · {project.key}
            </p>
          </div>
        </div>
      </div>

      <div className='flex flex-wrap gap-2'>
        <Button
          type='button'
          variant='outline'
          onClick={() => router.push(`/pm/projects/${projectId}/edit`)}
        >
          <PenLine className='mr-2 h-4 w-4' />
          Edit project
        </Button>
        <Button
          type='button'
          variant={project.isArchived ? 'secondary' : 'outline'}
          onClick={handleArchiveToggle}
          disabled={archiveState.isLoading || unarchiveState.isLoading}
        >
          <Archive className='mr-2 h-4 w-4' />
          {project.isArchived ? 'Unarchive' : 'Archive'}
        </Button>
      </div>
    </div>

    <div className='grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]'>
      <Card className='shadow-sm'>
        <CardHeader className='border-b'>
          <CardTitle className='text-base'>Project profile</CardTitle>
        </CardHeader>
        <CardContent className='grid gap-3 p-4 md:grid-cols-2'>
          <SettingField label='Name' value={project.name} />
          <SettingField label='Key' value={project.key} />
          <SettingField label='Lead' value={project.leadUserName || 'Unassigned'} />
          <SettingField label='Category' value={project.category?.name || 'No category'} />
          <SettingField label='Project type' value={project.projectTypeKey} />
          <SettingField label='State' value={project.isArchived ? 'Archived' : 'Active'} />
          <div className='md:col-span-2'>
            <SettingField label='Description' value={project.description || '-'} />
          </div>
        </CardContent>
      </Card>

      <Card className='shadow-sm'>
        <CardHeader className='border-b'>
          <CardTitle className='text-base'>People & roles</CardTitle>
        </CardHeader>
        <CardContent className='space-y-3 p-4'>
          <SettingField label='Project lead' value={people.leadUserName || 'Unassigned'} />
          <div className='grid gap-3 sm:grid-cols-2'>
            <SettingField label='Members' value={String(people.memberCount)} />
            <SettingField label='Roles used' value={String(people.roleCount)} />
          </div>
          <Button
            type='button'
            variant='outline'
            className='w-full justify-start'
            onClick={() => router.push(`/pm/projects/${projectId}/people`)}
          >
            <Users className='mr-2 h-4 w-4' />
            Open people
          </Button>
        </CardContent>
      </Card>
    </div>

    <div className='grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]'>
      <Card className='shadow-sm'>
        <CardHeader className='border-b'>
          <CardTitle className='text-base'>Components</CardTitle>
        </CardHeader>
        <CardContent className='p-0'>
          <div className='flex items-center justify-between border-b px-4 py-3 text-sm text-muted-foreground'>
            <span>{components.totalCount} total</span>
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() => router.push(`/pm/projects/${projectId}/components`)}
            >
              <FolderKanban className='mr-2 h-4 w-4' />
              Open components
            </Button>
          </div>
          {componentPreview.length > 0 ? (
            <div className='divide-y'>
              {componentPreview.map((component) => (
                <div
                  key={component.id}
                  className='grid gap-3 px-4 py-3 md:grid-cols-[minmax(0,1fr)_120px_160px]'
                >
                  <div className='min-w-0'>
                    <p className='font-medium'>{component.name}</p>
                    <p className='truncate text-sm text-muted-foreground'>
                      {component.description || '-'}
                    </p>
                  </div>
                  <div className='text-sm text-muted-foreground'>
                    {component.issueCount} issues
                  </div>
                  <div className='text-sm text-muted-foreground'>
                    {component.assigneeType}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className='px-4 py-6 text-sm text-muted-foreground'>
              No components.
            </div>
          )}
        </CardContent>
      </Card>

      <Card className='shadow-sm'>
        <CardHeader className='border-b'>
          <CardTitle className='text-base'>Configuration schemes</CardTitle>
        </CardHeader>
        <CardContent className='space-y-2 p-4'>
          {schemes.map((scheme) => (
            <div key={scheme.type} className='rounded-md border p-3'>
              <div className='flex items-start justify-between gap-3'>
                <div className='min-w-0'>
                  <p className='text-sm font-medium'>{scheme.label}</p>
                  <p className='truncate text-sm text-muted-foreground'>
                    {scheme.schemeName ||
                      (typeof scheme.schemeId === 'number'
                        ? `#${scheme.schemeId}`
                        : '-')}
                  </p>
                </div>
                {scheme.globalSection ? (
                  <Button asChild type='button' variant='ghost' size='sm'>
                    <Link href={`/pm/settings?section=${scheme.globalSection}`}>
                      Open
                    </Link>
                  </Button>
                ) : (
                  <Badge variant='secondary'>Read-only</Badge>
                )}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  </div>
);
```

- [ ] **Step 7: Ensure imports are correct**

`PMProjectSettingsPage.tsx` should import `Link` from `next/link`, `Badge`, `Button`, `Card`, `CardContent`, `CardHeader`, and `CardTitle` from shared UI, and icons `Archive`, `ArrowLeft`, `FolderKanban`, `PenLine`, `Users`.

- [ ] **Step 8: Run frontend checks**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all three commands pass.

- [ ] **Step 9: Commit**

```bash
git add serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx
git commit -m "feat(web): render project settings launcher"
```

---

### Task 6: Final Verification

**Files:**
- Verify: `pm_core`
- Verify: `serp_web`

- [ ] **Step 1: Run backend focused test**

Run from `pm_core`:

```bash
./mvnw.cmd -Dtest=GetProjectSettingsOverviewQueryHandlerTest test
```

Expected: tests pass.

- [ ] **Step 2: Run backend compile**

Run from `pm_core`:

```bash
./mvnw.cmd clean compile
```

Expected: compile succeeds.

- [ ] **Step 3: Run frontend verification**

Run from `serp_web`:

```bash
npm run lint
npm run type-check
npm run format:check
```

Expected: all checks pass.

- [ ] **Step 4: Inspect final diff**

Run from repo root:

```bash
git status --short
git diff --stat HEAD
```

Expected: only files from this plan are changed since the last task commit.

- [ ] **Step 5: Commit verification-only cleanup if needed**

If formatting modifies files, commit them:

```bash
git add pm_core/src/main/java/serp/project/pmcore/application/project/query/settings pm_core/src/main/java/serp/project/pmcore/ui/rest/project/ProjectController.java pm_core/src/test/java/serp/project/pmcore/application/project/query/settings serp_web/src/modules/pm/types/project-api.types.ts serp_web/src/modules/pm/api/projectApi.ts serp_web/src/modules/pm/pages/PMProjectSettingsPage.tsx
git commit -m "chore(pm): format project settings overview"
```

Expected: commit is created only if formatting produced changes.

---

## Self-Review

Spec coverage:

- Backend-composed endpoint: Task 1, Task 2, Task 3.
- Core configuration only: Task 2 and Task 5 render project, people, components, and schemes without readiness or operational metrics.
- Launcher-only UI: Task 5 links to project edit, people, components, and global settings sections without inline management.
- Section-level global links: Task 2 maps only `globalSection`; Task 5 links to `/pm/settings?section=<globalSection>`.
- No frontend permission gating: Task 5 always renders actions and relies on API errors.
- Error and fallback behavior: Task 5 loading/unavailable/fallback values; archive errors keep existing toast behavior.
- Verification: Task 6.

Placeholder scan:

- The plan contains no deferred implementation markers.
- Unsupported scheme families are explicitly handled as read-only.
- The optional formatting commit is conditional and names exact files.

Type consistency:

- Java view names in Task 1 match handler and controller tasks.
- TypeScript `PMProjectSettingsOverviewApi` matches Java response field names.
- RTK Query hook name in Task 4 matches the import used by Task 5.
