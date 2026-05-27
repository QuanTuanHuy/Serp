/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowMapper;
import serp.project.pmcore.infrastructure.store.model.WorkflowModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowAdapter implements IWorkflowPort {

    private final IWorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    @Override
    public WorkflowEntity createWorkflow(WorkflowEntity workflow) {
        return workflowMapper.toEntity(workflowRepository.save(workflowMapper.toModel(workflow)));
    }

    @Override
    public void updateWorkflow(WorkflowEntity workflow) {
        workflowRepository.save(workflowMapper.toModel(workflow));
    }

    @Override
    public Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId) {
        return workflowRepository.findByIdAndTenantId(id, tenantId)
                .map(workflowMapper::toEntity);
    }

    @Override
    public Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId) {
        return workflowRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(workflowMapper::toEntity);
    }

    @Override
    public Optional<WorkflowEntity> getWorkflowByWorkflowKey(Long tenantId, String workflowKey) {
        return workflowRepository.findFirstByTenantIdAndWorkflowKeyOrderByIdAsc(tenantId, workflowKey)
                .map(workflowMapper::toEntity);
    }

    @Override
    public PageResult<WorkflowEntity> listWorkflowsIncludingSystem(Long tenantId, WorkflowListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<WorkflowModel> result = workflowRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearchPatternLower(),
                criteria.getIsActive(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(workflowMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public List<WorkflowEntity> getWorkflowsByIds(List<Long> workflowIds, Long tenantId) {
        return workflowMapper.toEntities(workflowRepository.findAllByIdInAndTenantId(workflowIds, tenantId));
    }

    private Sort resolveSort(WorkflowListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "workflow_key" -> Sort.by(
                    new Sort.Order(direction, "workflowKey"),
                    new Sort.Order(direction, "id")
            );
            case "updated_at" -> Sort.by(
                    new Sort.Order(direction, "updatedAt"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException(
                    "sortBy must be one of name, workflow_key, updated_at, created_at"
            );
        };
    }
}
