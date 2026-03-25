/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;
import serp.project.pmcore.domain.enums.ProjectPermissionGranteeType;
import serp.project.pmcore.domain.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.port.store.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectPermissionEvaluationService implements IProjectPermissionEvaluationService {

    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;

    @Override
    public boolean hasPermission(ProjectEntity project,
                                 ProjectPermissionEvaluationContext context,
                                 String permissionKey) {
        if (project == null || context == null || permissionKey == null || permissionKey.isBlank()) {
            return false;
        }

        if (project.getPermissionSchemeId() == null) {
            return isFallbackProjectLeadGrant(project, context, permissionKey);
        }

        List<PermissionSchemeEntryEntity> entries = permissionSchemeEntryPort
                .getPermissionSchemeEntriesBySchemeId(project.getPermissionSchemeId(), project.getTenantId());

        return entries.stream()
                .filter(entry -> permissionKey.equalsIgnoreCase(entry.getPermissionKey()))
                .anyMatch(entry -> matchesEntry(project, context, entry));
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
            return false;
        }

        return switch (granteeType) {
            case USER -> Objects.equals(String.valueOf(context.getUserId()), entry.getGranteeRef());
            case GROUP -> matchesAnyGroup(context.getGroupKeys(), entry.getGranteeRef());
            case PROJECT_LEAD -> Objects.equals(project.getLeadUserId(), context.getUserId());
            case REPORTER -> Objects.equals(context.getReporterUserId(), context.getUserId());
            case ASSIGNEE -> Objects.equals(context.getAssigneeUserId(), context.getUserId());
            case ANY_LOGGED_IN_USER, AUTHENTICATED -> context.getUserId() != null;
            case PROJECT_ROLE -> matchesProjectRoleGrant(project, context, entry.getGranteeRef());
        };
    }

    private boolean matchesProjectRoleGrant(ProjectEntity project,
                                            ProjectPermissionEvaluationContext context,
                                            String roleName) {
        if (roleName == null || roleName.isBlank() || context.getUserId() == null) {
            return false;
        }

        ProjectRoleEntity role = projectRoleService.getProjectRoleByNameIncludingSystem(roleName, project.getTenantId())
                .orElse(null);
        if (role == null) {
            return false;
        }

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

        return false;
    }

    private boolean matchesAnyGroup(Set<String> groupKeys, String granteeRef) {
        if (granteeRef == null || granteeRef.isBlank()) {
            return false;
        }
        return safeGroupKeys(groupKeys).contains(granteeRef);
    }

    private Set<String> safeGroupKeys(Set<String> groupKeys) {
        return groupKeys == null ? Collections.emptySet() : groupKeys;
    }

    private ProjectPermissionGranteeType parseGranteeType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PROJECT_ROLE" -> ProjectPermissionGranteeType.PROJECT_ROLE;
            case "GROUP" -> ProjectPermissionGranteeType.GROUP;
            case "USER" -> ProjectPermissionGranteeType.USER;
            case "PROJECT_LEAD" -> ProjectPermissionGranteeType.PROJECT_LEAD;
            case "REPORTER" -> ProjectPermissionGranteeType.REPORTER;
            case "ASSIGNEE" -> ProjectPermissionGranteeType.ASSIGNEE;
            case "ANY_LOGGED_IN_USER", "LOGGED_IN_USER" -> ProjectPermissionGranteeType.ANY_LOGGED_IN_USER;
            case "AUTHENTICATED" -> ProjectPermissionGranteeType.AUTHENTICATED;
            default -> null;
        };
    }
}
