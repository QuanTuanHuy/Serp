/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectPermissionGranteeType;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectPermissionEvaluationService implements IProjectPermissionEvaluationService {

    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;

    @Override
    public boolean hasPermission(ProjectEntity project,
                                 ProjectPermissionEvaluationContext context,
                                 String permissionKey) {
        if (project == null || context == null || permissionKey == null || permissionKey.isBlank()) {
            log.debug("Permission check skipped due to invalid input: project={}, context={}, permissionKey={} ",
                    project == null ? "null" : project.getId(),
                    context == null ? "null" : context.getUserId(),
                    permissionKey);
            return false;
        }

        if (project.getPermissionSchemeId() == null) {
            return isFallbackProjectLeadGrant(project, context, permissionKey);
        }

        List<PermissionSchemeEntryEntity> entries = permissionSchemeEntryPort
                .getPermissionSchemeEntriesBySchemeId(project.getPermissionSchemeId(), project.getTenantId());

        boolean granted = entries.stream()
                .filter(entry -> permissionKey.equalsIgnoreCase(entry.getPermissionKey()))
                .anyMatch(entry -> matchesEntry(project, context, entry));

        if (!granted) {
            log.debug("Permission denied by scheme evaluation: projectId={}, tenantId={}, userId={}, permissionKey={}",
                    project.getId(), project.getTenantId(), context.getUserId(), permissionKey);
        }

        return granted;
    }

    @Override
    public void checkPermission(ProjectEntity project,
                                ProjectPermissionEvaluationContext context,
                                String permissionKey) {
        if (!hasPermission(project, context, permissionKey)) {
            throw AccessDeniedException.projectPermission(permissionKey, project.getId());
        }
    }

    private boolean isFallbackProjectLeadGrant(ProjectEntity project,
                                               ProjectPermissionEvaluationContext context,
                                               String permissionKey) {
        return ProjectPermissionKeys.ADMINISTER_PROJECTS.equalsIgnoreCase(permissionKey)
                && Objects.equals(project.getLeadUserId(), context.getUserId());
    }

    private boolean matchesEntry(ProjectEntity project,
                                 ProjectPermissionEvaluationContext context,
                                 PermissionSchemeEntryEntity entry) {
        ProjectPermissionGranteeType granteeType = parseGranteeType(entry.getGranteeType());
        if (granteeType == null) {
            log.debug("Unsupported project permission grantee type: rawValue={}, entryId={}",
                    entry.getGranteeType(), entry.getId());
            return false;
        }

        return switch (granteeType) {
            case USER -> Objects.equals(String.valueOf(context.getUserId()), entry.getGranteeRef());
            case GROUP -> matchesAnyGroup(context.getGroupKeys(), entry.getGranteeRef());
            case PROJECT_LEAD -> Objects.equals(project.getLeadUserId(), context.getUserId());
            case REPORTER -> Objects.equals(context.getReporterUserId(), context.getUserId());
            case ASSIGNEE -> Objects.equals(context.getAssigneeUserId(), context.getUserId());
            case ANY_LOGGED_IN_USER, AUTHENTICATED -> context.getUserId() != null;
            case APPLICATION_ACCESS, ANYONE_ON_WEB, USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE -> false;
            case PROJECT_ROLE -> matchesProjectRoleGrant(project, context, entry.getGranteeRef());
        };
    }

    private boolean matchesProjectRoleGrant(ProjectEntity project,
                                            ProjectPermissionEvaluationContext context,
                                            String roleName) {
        if (roleName == null || roleName.isBlank() || context.getUserId() == null) {
            return false;
        }

        List<ProjectRoleEntity> roles = projectRoleService.getProjectRolesByNameIncludingSystem(roleName, project.getTenantId());
        if (roles.isEmpty()) {
            log.debug("No project role found for grant resolution: roleName={}, projectId={}, tenantId={}",
                    roleName, project.getId(), project.getTenantId());
            return false;
        }

        for (ProjectRoleEntity role : roles) {
            if (projectRoleActorService.hasRoleAssignment(
                    project.getTenantId(),
                    project.getId(),
                    role.getId(),
                    ProjectRoleActorSubjectType.USER.name(),
                    String.valueOf(context.getUserId())
            )) {
                return true;
            }

            for (String groupKey : safeGroupKeys(context.getGroupKeys())) {
                if (projectRoleActorService.hasRoleAssignment(
                        project.getTenantId(),
                        project.getId(),
                        role.getId(),
                        ProjectRoleActorSubjectType.GROUP.name(),
                        groupKey
                )) {
                    return true;
                }
            }
        }

        log.debug("Project role grant did not match actor assignments: roleName={}, candidateRoleIds={}, projectId={}, userId={}, groups={}",
                roleName,
                roles.stream().map(ProjectRoleEntity::getId).collect(Collectors.toList()),
                project.getId(),
                context.getUserId(),
                safeGroupKeys(context.getGroupKeys()));

        return false;
    }

    private boolean matchesAnyGroup(Set<String> groupKeys, String granteeRef) {
        if (granteeRef == null || granteeRef.isBlank()) {
            return false;
        }

        String normalizedGranteeRef = normalizeToken(granteeRef);
        if (normalizedGranteeRef == null) {
            return false;
        }

        return safeGroupKeys(groupKeys).contains(normalizedGranteeRef);
    }

    private Set<String> safeGroupKeys(Set<String> groupKeys) {
        if (groupKeys == null || groupKeys.isEmpty()) {
            return Collections.emptySet();
        }

        return groupKeys.stream()
                .map(this::normalizeToken)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private ProjectPermissionGranteeType parseGranteeType(String rawValue) {
        return ProjectPermissionGranteeType.fromValue(rawValue);
    }

    private String normalizeToken(String token) {
        if (token == null) {
            return null;
        }

        String normalized = token.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }
}
