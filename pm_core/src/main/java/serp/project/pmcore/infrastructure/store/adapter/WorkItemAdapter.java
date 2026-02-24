/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.dto.filter.WorkItemFilterRequest;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;
import serp.project.pmcore.core.port.store.IWorkItemPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemRowMapper;
import serp.project.pmcore.infrastructure.store.query.WorkItemQueryBuilder;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkItemAdapter implements IWorkItemPort {

    private final IWorkItemRepository workItemRepository;
    private final WorkItemMapper workItemMapper;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final WorkItemQueryBuilder queryBuilder;
    private final WorkItemRowMapper rowMapper;

    @Override
    public WorkItemEntity saveWorkItem(WorkItemEntity workItem) {
        return workItemMapper.toEntity(
                workItemRepository.save(workItemMapper.toModel(workItem))
        );
    }

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
    public void deleteWorkItemById(Long id, Long tenantId) {
        workItemRepository.deleteByIdAndTenantId(id, tenantId);
    }

    @Override
    public Pair<List<WorkItemEntity>, Long> searchWorkItems(Long tenantId, WorkItemFilterRequest filter) {
        var qr = queryBuilder.build(tenantId, filter);
        log.debug("WorkItem search SQL: {}", qr.dataSql());
        log.debug("WorkItem count SQL: {}", qr.countSql());

        List<WorkItemEntity> data = jdbcTemplate.query(qr.dataSql(), qr.params(), rowMapper);
        Long total = jdbcTemplate.queryForObject(qr.countSql(), qr.params(), Long.class);
        return Pair.of(data, total);
    }
}
