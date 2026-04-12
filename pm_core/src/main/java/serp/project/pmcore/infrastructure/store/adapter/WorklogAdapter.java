/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.port.IWorklogPort;
import serp.project.pmcore.domain.worklog.query.WorklogListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.WorklogMapper;
import serp.project.pmcore.infrastructure.store.model.WorklogModel;
import serp.project.pmcore.infrastructure.store.repository.IWorklogRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorklogAdapter implements IWorklogPort {

    private final IWorklogRepository worklogRepository;
    private final WorklogMapper worklogMapper;

    @Override
    public WorklogEntity saveWorklog(WorklogEntity worklog) {
        return worklogMapper.toEntity(worklogRepository.save(worklogMapper.toModel(worklog)));
    }

    @Override
    public Optional<WorklogEntity> getWorklogById(Long worklogId, Long tenantId) {
        return worklogRepository.findByIdAndTenantId(worklogId, tenantId)
                .map(worklogMapper::toEntity);
    }

    @Override
    public PageResult<WorklogEntity> listWorklogs(Long tenantId, WorklogListCriteria criteria) {
        Pageable pageable = PageRequest.of(resolvePage(criteria), resolvePageSize(criteria), resolveSort(criteria));
        Page<WorklogModel> result = worklogRepository.findAllByWorkItemIdWithFilters(
                tenantId,
                criteria.getWorkItemId(),
                criteria.getAuthorId(),
                pageable
        );
        return new PageResult<>(worklogMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public long sumActiveTimeSpentByWorkItemId(Long workItemId, Long tenantId) {
        Long total = worklogRepository.sumActiveTimeSpentByWorkItemId(workItemId, tenantId);
        return total == null ? 0L : total;
    }

    private int resolvePage(WorklogListCriteria criteria) {
        int page = criteria.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolvePageSize(WorklogListCriteria criteria) {
        int pageSize = criteria.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private Sort resolveSort(WorklogListCriteria criteria) {
        Sort.Direction direction = switch (criteria.getSortDirection().toUpperCase()) {
            case "ASC" -> Sort.Direction.ASC;
            case "DESC" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("sortDirection must be ASC or DESC");
        };

        return switch (criteria.getSortBy().toLowerCase()) {
            case "start_date" -> Sort.by(
                    new Sort.Order(direction, "startDate"),
                    new Sort.Order(direction, "id")
            );
            case "time_spent" -> Sort.by(
                    new Sort.Order(direction, "timeSpent"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of start_date, time_spent, created_at");
        };
    }
}
