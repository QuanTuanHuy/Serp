/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.permission.ProjectPermissionDefinitionView;
import serp.project.pmcore.application.project.permission.ProjectPermissionGrantView;
import serp.project.pmcore.application.project.permission.ProjectPermissionSchemeView;
import serp.project.pmcore.application.project.permission.query.ProjectPermissionSettingsView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.permission.port.IPermissionDefinitionPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemePort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.cloner.PermissionSchemeCloner;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.ProjectPermissionGranteeType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainException;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplaceProjectPermissionGrantsCommandHandler
        implements ICommandHandler<ReplaceProjectPermissionGrantsCommand, ProjectPermissionSettingsView> {

    private final IProjectService projectService;
    private final IProjectReadPort projectReadPort;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IPermissionSchemePort permissionSchemePort;
    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;
    private final IPermissionDefinitionPort permissionDefinitionPort;
    private final PermissionSchemeCloner permissionSchemeCloner;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectPermissionSettingsView handle(ReplaceProjectPermissionGrantsCommand command) {
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(command.userId())
                        .groupKeys(command.groupKeys())
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        PermissionSchemeEntity scheme = loadProjectPermissionScheme(project, command.tenantId());
        PermissionSchemeEntity editableScheme = ensureEditableScheme(project, scheme, command);
        List<PermissionDefinitionEntity> definitions = permissionDefinitionPort
                .getPermissionDefinitionsIncludingSystem(command.tenantId());

        List<PermissionSchemeEntryEntity> replacementEntries = buildReplacementEntries(
                editableScheme.getId(),
                command.tenantId(),
                command.userId(),
                command.grants(),
                definitions
        );

        permissionSchemeEntryPort.deletePermissionSchemeEntriesBySchemeId(
                editableScheme.getId(),
                command.tenantId(),
                command.userId()
        );
        List<PermissionSchemeEntryEntity> savedEntries =
                permissionSchemeEntryPort.createPermissionSchemeEntries(replacementEntries);

        return new ProjectPermissionSettingsView(
                ProjectPermissionSchemeView.from(editableScheme, command.tenantId()),
                definitions.stream().map(ProjectPermissionDefinitionView::from).toList(),
                savedEntries.stream().map(ProjectPermissionGrantView::from).toList()
        );
    }

    private PermissionSchemeEntity loadProjectPermissionScheme(ProjectEntity project, Long tenantId) {
        Long schemeId = project.getPermissionSchemeId();
        if (schemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                    "Project has no permission scheme: projectId=" + project.getId()
            );
        }
        return permissionSchemePort.getPermissionSchemeByIdIncludingSystem(schemeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                        "Permission scheme not found: id=" + schemeId
                ));
    }

    private PermissionSchemeEntity ensureEditableScheme(ProjectEntity project,
                                                        PermissionSchemeEntity scheme,
                                                        ReplaceProjectPermissionGrantsCommand command) {
        boolean tenantOwned = scheme.getTenantId() != null && scheme.getTenantId().equals(command.tenantId());
        boolean sharedByMultipleProjects = tenantOwned
                && projectReadPort.countActiveProjectsByPermissionSchemeId(scheme.getId(), command.tenantId()) > 1;
        if (tenantOwned && !sharedByMultipleProjects) {
            return scheme;
        }

        if (permissionSchemeCloner == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Permission scheme cloner is not available"
            );
        }

        Long clonedSchemeId = permissionSchemeCloner.clonePermissionScheme(
                scheme,
                command.tenantId(),
                command.userId(),
                CloneMode.CLONE,
                ProvisioningExecutionContext.builder()
                        .projectId(project.getId())
                        .projectKey(project.getKey())
                        .build()
        );
        project.setPermissionSchemeId(clonedSchemeId);
        projectService.saveProject(project, command.userId());
        return permissionSchemePort.getPermissionSchemeById(clonedSchemeId, command.tenantId())
                .orElse(PermissionSchemeEntity.builder()
                        .id(clonedSchemeId)
                        .tenantId(command.tenantId())
                        .name(scheme.getName())
                        .description(scheme.getDescription())
                        .build());
    }

    private List<PermissionSchemeEntryEntity> buildReplacementEntries(Long schemeId,
                                                                      Long tenantId,
                                                                      Long userId,
                                                                      List<PermissionGrantData> requestedGrants,
                                                                      List<PermissionDefinitionEntity> definitions) {
        if (requestedGrants == null || requestedGrants.isEmpty()) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "At least one permission grant is required"
            );
        }

        Map<String, PermissionDefinitionEntity> definitionsByKey = definitions.stream()
                .collect(Collectors.toMap(
                        definition -> normalizeRequired(definition.getPermissionKey(), "permissionKey"),
                        Function.identity(),
                        (left, right) -> left
                ));

        long now = System.currentTimeMillis();
        Set<String> uniqueGrantKeys = new LinkedHashSet<>();
        List<PermissionSchemeEntryEntity> entries = new ArrayList<>(requestedGrants.size());
        boolean hasAdminGrant = false;

        for (PermissionGrantData requestedGrant : requestedGrants) {
            String permissionKey = normalizeRequired(requestedGrant.permissionKey(), "permissionKey");
            if (!definitionsByKey.containsKey(permissionKey)) {
                throw new ResourceNotFoundException(
                        DomainErrorCode.PERMISSION_KEY_NOT_FOUND,
                        "Permission definition not found: permissionKey=" + permissionKey
                );
            }

            ProjectPermissionGranteeType granteeType = ProjectPermissionGranteeType.fromValue(requestedGrant.granteeType());
            if (granteeType == null) {
                throw new DomainValidationException(
                        DomainErrorCode.BAD_REQUEST,
                        "Unsupported permission grantee type: " + requestedGrant.granteeType()
                );
            }

            String granteeRef = normalizeOptional(requestedGrant.granteeRef());
            Long customFieldId = requestedGrant.customFieldId();
            validateGrantee(granteeType, granteeRef, customFieldId);

            String uniqueGrantKey = permissionKey + "|" + granteeType.name() + "|"
                    + (granteeRef == null ? "" : granteeRef.toLowerCase(Locale.ROOT)) + "|"
                    + (customFieldId == null ? "" : customFieldId);
            if (!uniqueGrantKeys.add(uniqueGrantKey)) {
                throw new BusinessRuleViolationException(DomainErrorCode.DUPLICATE_PERMISSION_ENTRY);
            }

            if (ProjectPermissionKeys.ADMINISTER_PROJECTS.equals(permissionKey)) {
                hasAdminGrant = true;
            }

            entries.add(PermissionSchemeEntryEntity.builder()
                    .tenantId(tenantId)
                    .schemeId(schemeId)
                    .permissionKey(permissionKey)
                    .granteeType(granteeType.name())
                    .granteeRef(granteeRef)
                    .customFieldId(customFieldId)
                    .createdAt(now)
                    .createdBy(userId)
                    .updatedAt(now)
                    .updatedBy(userId)
                    .build());
        }

        if (!hasAdminGrant) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "At least one ADMINISTER_PROJECTS grant is required"
            );
        }

        return entries;
    }

    private void validateGrantee(ProjectPermissionGranteeType granteeType, String granteeRef, Long customFieldId) {
        switch (granteeType) {
            case PROJECT_ROLE, GROUP, USER, APPLICATION_ACCESS -> {
                if (granteeRef == null) {
                    throw new DomainValidationException(
                            DomainErrorCode.BAD_REQUEST,
                            granteeType.name() + " grants require granteeRef"
                    );
                }
            }
            case USER_CUSTOM_FIELD_VALUE, GROUP_CUSTOM_FIELD_VALUE -> {
                if (customFieldId == null || customFieldId <= 0) {
                    throw new DomainValidationException(
                            DomainErrorCode.BAD_REQUEST,
                            granteeType.name() + " grants require customFieldId"
                    );
                }
            }
            default -> {
                if (granteeRef != null) {
                    throw new DomainValidationException(
                            DomainErrorCode.BAD_REQUEST,
                            granteeType.name() + " grants must not include granteeRef"
                    );
                }
            }
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new DomainValidationException(DomainErrorCode.BAD_REQUEST, fieldName + " is required");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
