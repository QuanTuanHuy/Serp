/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemRowMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.infrastructure.store.query.WorkItemQueryBuilder;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemReadAdapter implements IWorkItemReadPort {

    private final IWorkItemRepository workItemRepository;
    private final WorkItemMapper workItemMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final WorkItemQueryBuilder queryBuilder;
    private final WorkItemRowMapper rowMapper;

    @Override
    public Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId) {
        return workItemRepository.findByIdAndTenantId(id, tenantId)
                .map(workItemMapper::toEntity);
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndProjectId(tenantId, projectId)
        );
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndIssueTypeId(tenantId, issueTypeId)
        );
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByPriorityId(Long priorityId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndPriorityId(tenantId, priorityId)
        );
    }

    @Override
    public List<Long> getActiveIssueTypeIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> issueTypeIds) {
        if (projectIds == null || projectIds.isEmpty() || issueTypeIds == null || issueTypeIds.isEmpty()) {
            return List.of();
        }
        return workItemRepository.findDistinctIssueTypeIdsInUseByProjectIds(tenantId, projectIds, issueTypeIds);
    }

    @Override
    public List<Long> getActivePriorityIdsInUseByProjectIds(Long tenantId, List<Long> projectIds, List<Long> priorityIds) {
        if (projectIds == null || projectIds.isEmpty() || priorityIds == null || priorityIds.isEmpty()) {
            return List.of();
        }
        return workItemRepository.findDistinctPriorityIdsInUseByProjectIds(tenantId, projectIds, priorityIds);
    }

    @Override
    public Optional<String> getLastRankByProjectId(Long projectId, Long tenantId) {
        return workItemRepository.findFirstByTenantIdAndProjectIdOrderByRankDescIdDesc(tenantId, projectId)
                .map(WorkItemModel::getRank);
    }

    @Override
    public PageResult<WorkItemEntity> searchWorkItems(Long tenantId, WorkItemSearchCriteria criteria) {
        var qr = queryBuilder.build(tenantId, criteria);
        log.debug("WorkItem search SQL: {}", qr.dataSql());
        log.debug("WorkItem count SQL: {}", qr.countSql());

        List<WorkItemEntity> data = jdbcTemplate.query(qr.dataSql(), qr.params(), rowMapper);
        Long total = jdbcTemplate.queryForObject(qr.countSql(), qr.params(), Long.class);
        return new PageResult<>(data, total != null ? total : 0L);
    }

    @Override
    public Optional<WorkItemDetailProjection> getWorkItemDetailById(Long id, Long tenantId) {
        return workItemRepository.findWorkItemDetailById(id, tenantId);
    }

    @Override
    public List<WorkItemEntity> getActiveChildrenByParentId(Long parentId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndParentId(tenantId, parentId)
        );
    }
}
