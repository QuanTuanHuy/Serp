/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.people.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
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
import serp.project.pmcore.domain.user.service.IUserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListProjectPeopleQueryHandler implements IQueryHandler<ListProjectPeopleQuery, List<ProjectPeopleView>> {

    private static final String USER_SUBJECT_TYPE = "USER";

    private final IProjectService projectService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectRoleService projectRoleService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IUserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectPeopleView> handle(ListProjectPeopleQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.BROWSE_PROJECTS
        );

        Map<Long, List<ProjectRoleActorEntity>> actorsByUserId = projectRoleActorService
                .getActorsByProject(query.projectId(), query.tenantId())
                .stream()
                .filter(actor -> USER_SUBJECT_TYPE.equalsIgnoreCase(actor.getSubjectType()))
                .filter(actor -> parseUserId(actor.getSubjectId()) != null)
                .collect(Collectors.groupingBy(
                        actor -> parseUserId(actor.getSubjectId()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        if (project.getLeadUserId() != null) {
            actorsByUserId.putIfAbsent(project.getLeadUserId(), List.of());
        }

        List<Long> userIds = new ArrayList<>(actorsByUserId.keySet());
        Map<Long, UserProfileDto> usersById = userService.getUserProfilesByIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserProfileDto::getId, Function.identity(), (left, right) -> left));

        Map<Long, ProjectRoleEntity> rolesById = actorsByUserId.values()
                .stream()
                .flatMap(List::stream)
                .map(ProjectRoleActorEntity::getProjectRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        roleId -> projectRoleService.getProjectRoleByIdIncludingSystem(roleId, query.tenantId()),
                        (left, right) -> left
                ));

        return actorsByUserId.entrySet()
                .stream()
                .map(entry -> toView(entry.getKey(), entry.getValue(), usersById.get(entry.getKey()), rolesById, project))
                .sorted(Comparator.comparing(ProjectPeopleView::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(ProjectPeopleView::email, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(ProjectPeopleView::userId))
                .toList();
    }

    private ProjectPeopleView toView(Long userId,
                                     List<ProjectRoleActorEntity> actors,
                                     UserProfileDto profile,
                                     Map<Long, ProjectRoleEntity> rolesById,
                                     ProjectEntity project) {
        List<ProjectPeopleView.RoleView> roles = actors.stream()
                .map(actor -> rolesById.get(actor.getProjectRoleId()))
                .filter(Objects::nonNull)
                .map(role -> new ProjectPeopleView.RoleView(
                        role.getId(),
                        role.getName(),
                        Boolean.TRUE.equals(role.getIsSystem())
                ))
                .distinct()
                .sorted(Comparator.comparing(ProjectPeopleView.RoleView::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        Long addedAt = actors.stream()
                .map(ProjectRoleActorEntity::getCreatedAt)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(null);

        return new ProjectPeopleView(
                userId,
                resolveName(userId, profile),
                profile == null ? null : profile.getEmail(),
                profile == null ? null : profile.getAvatarUrl(),
                Objects.equals(project.getLeadUserId(), userId),
                roles,
                addedAt
        );
    }

    private String resolveName(Long userId, UserProfileDto profile) {
        if (profile == null) {
            return "User #" + userId;
        }
        String fullName = profile.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return profile.getEmail() == null || profile.getEmail().isBlank() ? "User #" + userId : profile.getEmail();
    }

    private Long parseUserId(String subjectId) {
        try {
            return Long.parseLong(subjectId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();
    }
}
