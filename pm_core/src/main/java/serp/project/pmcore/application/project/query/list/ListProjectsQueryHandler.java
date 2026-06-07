/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.query.ProjectListCriteria;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;

@Service
@RequiredArgsConstructor
public class ListProjectsQueryHandler implements IQueryHandler<ListProjectsQuery, PageView<ProjectSummaryView>> {

    private final IProjectReadPort projectReadPort;
    private final IUserService userService;
    private final IProjectCategoryService projectCategoryService;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProjectSummaryView> handle(ListProjectsQuery query) {
        ProjectListCriteria criteria = query.toCriteria();
        PageResult<ProjectEntity> projects = projectReadPort.getProjects(
                query.tenantId(),
                query.userId(),
                query.groupKeys(),
                criteria.getSearch(),
                criteria.getCategoryId(),
                criteria.getProjectTypeKey(),
                criteria.getArchived(),
                criteria.getPage(),
                criteria.getPageSize(),
                criteria.getSortBy(),
                criteria.getSortDirection());
        if (projects.items().isEmpty()) {
            return new PageView<>(List.of(), 0, 0, criteria.getPage(), criteria.getPageSize());
        }

        List<Long> leadUserIds = projects.items().stream()
                .map(ProjectEntity::getLeadUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> leadUserNameMap = resolveLeadUserNames(leadUserIds);

        List<Long> categoryIds = projects.items().stream()
                .map(ProjectEntity::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> categoryNameMap = resolveCategoryNames(categoryIds);

        PageResult<ProjectSummaryView> result = projects.map(project -> ProjectSummaryView.from(project,
                nullableMapGet(leadUserNameMap, project.getLeadUserId()),
                nullableMapGet(categoryNameMap, project.getCategoryId())));

        return PageViews.from(result, criteria);
    }

    private Map<Long, String> resolveCategoryNames(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        List<ProjectCategoryEntity> categories = projectCategoryService.getCategoriesByIds(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(ProjectCategoryEntity::getId, ProjectCategoryEntity::getName));
    }

    private Map<Long, String> resolveLeadUserNames(List<Long> leadUserIds) {
        if (leadUserIds == null || leadUserIds.isEmpty()) {
            return Map.of();
        }
        List<UserProfileDto> leadUserProfiles = userService.getUserProfilesByIds(leadUserIds);
        return leadUserProfiles.stream()
                .collect(Collectors.toMap(UserProfileDto::getId, UserProfileDto::getFullName, (left, right) -> left));
    }

    private static String nullableMapGet(Map<Long, String> map, Long key) {
        return key == null ? null : map.get(key);
    }
}
