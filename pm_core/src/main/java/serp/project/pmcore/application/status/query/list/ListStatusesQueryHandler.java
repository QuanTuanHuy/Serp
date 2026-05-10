/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListStatusesQueryHandler implements IQueryHandler<ListStatusesQuery, PageView<StatusView>> {

    private final IStatusService statusService;

    private final IStatusPort statusPort;
    private final IProjectReadPort projectReadPort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowStepPort workflowStepPort;
    
    @Override
    @Transactional(readOnly = true)
    public PageView<StatusView> handle(ListStatusesQuery query) {
        StatusListCriteria criteria = query.toCriteria();
        if (query.projectId() != null) {
            return PageViews.from(
                    listProjectStatuses(query, criteria),
                    criteria,
                    status -> StatusView.from(status, Boolean.TRUE.equals(status.getIsSystem()))
            );
        }
        return PageViews.from(
                statusService.listVisibleStatuses(query.tenantId(), criteria),
                criteria,
                status -> StatusView.from(status, Boolean.TRUE.equals(status.getIsSystem()))
        );
    }

    private PageResult<StatusEntity> listProjectStatuses(ListStatusesQuery query,
                                                        StatusListCriteria criteria) {
        ProjectEntity project = projectReadPort.getProjectById(query.projectId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.project(query.projectId()));
        if (project.getWorkflowSchemeId() == null) {
            return new PageResult<>(List.of(), 0);
        }

        WorkflowSchemeEntity workflowScheme = workflowSchemePort.getWorkflowSchemeById(project.getWorkflowSchemeId(), query.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.workflowScheme(project.getWorkflowSchemeId()));
        Set<Long> workflowIds = new HashSet<>();
        if (workflowScheme.getDefaultWorkflowId() != null) {
            workflowIds.add(workflowScheme.getDefaultWorkflowId());
        }
        List<WorkflowSchemeItemEntity> schemeItems = workflowSchemeItemPort
                .getWorkflowSchemeItemsBySchemeIdIncludingSystem(project.getWorkflowSchemeId(), query.tenantId());
        for (WorkflowSchemeItemEntity schemeItem : schemeItems) {
            workflowIds.add(schemeItem.getWorkflowId());
        }
        List<WorkflowEntity> workflows = workflowPort.getWorkflowsByIds(new ArrayList<>(workflowIds), query.tenantId());
        LinkedHashSet<Long> statusIds = new LinkedHashSet<>();
        for (WorkflowEntity workflow : workflows) {
            if (workflow.getCurrentPublishedVersionId() == null) {
                continue;
            }
            workflowStepPort.getWorkflowStepsByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), query.tenantId())
                    .stream()
                    .map(WorkflowStepEntity::getStatusId)
                    .forEach(statusIds::add);
        }
        List<StatusEntity> filtered = statusPort.getStatusesByIds(new ArrayList<>(statusIds), query.tenantId())
                .stream()
                .filter(status -> criteria.getStatusCategoryId() == null
                        || criteria.getStatusCategoryId().equals(status.getCategoryId()))
                .filter(status -> matchesSearch(status.getName(), status.getStatusKey(), criteria.getSearch()))
                .sorted(Comparator.comparing(StatusEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        return page(filtered, criteria);
    }

    private boolean matchesSearch(String name, String key, String search) {
        if (search == null) {
            return true;
        }
        String needle = search.toLowerCase(Locale.ROOT);
        return contains(name, needle) || contains(key, needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private <T> PageResult<T> page(List<T> items, StatusListCriteria criteria) {
        int page = Math.max(criteria.getPage(), 0);
        int pageSize = Math.max(criteria.getPageSize(), 1);
        int fromIndex = Math.min(page * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageResult<>(items.subList(fromIndex, toIndex), items.size());
    }
}
