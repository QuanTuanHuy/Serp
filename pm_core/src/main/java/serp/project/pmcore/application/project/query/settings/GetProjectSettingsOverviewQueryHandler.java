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
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
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
    private final IProjectComponentService projectComponentService;
    private final IProjectCategoryPort projectCategoryPort;
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
        PeopleSummary peopleSummary = buildPeopleSummary(project, query.tenantId());
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
                toProjectView(project, query.tenantId(), leadUserName),
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

    private ProjectSettingsOverviewView.ProjectView toProjectView(ProjectEntity project,
                                                                   Long tenantId,
                                                                   String leadUserName) {
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
                resolveCategory(project.getCategoryId(), tenantId)
        );
    }

    private ProjectSettingsOverviewView.CategoryView resolveCategory(Long categoryId, Long tenantId) {
        if (categoryId == null) {
            return null;
        }
        return projectCategoryPort.getCategoryByIdIncludingSystem(categoryId, tenantId)
                .map(this::toCategoryView)
                .orElse(null);
    }

    private ProjectSettingsOverviewView.CategoryView toCategoryView(ProjectCategoryEntity category) {
        return new ProjectSettingsOverviewView.CategoryView(category.getId(), category.getName());
    }

    private PeopleSummary buildPeopleSummary(ProjectEntity project, Long tenantId) {
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
