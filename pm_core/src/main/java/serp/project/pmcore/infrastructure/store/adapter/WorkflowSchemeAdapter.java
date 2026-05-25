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
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowSchemeMapper;
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowSchemeItemRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowSchemeRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowSchemeAdapter implements IWorkflowSchemePort {

    private final IWorkflowSchemeRepository workflowSchemeRepository;
    private final IWorkflowSchemeItemRepository workflowSchemeItemRepository;
    private final WorkflowSchemeMapper workflowSchemeMapper;
    private final WorkflowSchemeItemMapper workflowSchemeItemMapper;

    @Override
    public WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme) {
        return workflowSchemeMapper.toEntity(
                workflowSchemeRepository.save(workflowSchemeMapper.toModel(scheme))
        );
    }

    @Override
    public Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId) {
        return workflowSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(workflowSchemeMapper::toEntity);
    }

    @Override
    public Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return workflowSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(workflowSchemeMapper::toEntity);
    }

    @Override
    public Optional<WorkflowSchemeEntity> getWorkflowSchemeWithItems(Long schemeId, Long tenantId) {
        return workflowSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(model -> {
                    WorkflowSchemeEntity scheme = workflowSchemeMapper.toEntity(model);
                    scheme.setItems(workflowSchemeItemMapper.toEntities(
                            workflowSchemeItemRepository.findAllByTenantIdAndSchemeIdOrderByIdAsc(tenantId, schemeId)
                    ));
                    return scheme;
                });
    }

    @Override
    public List<WorkflowSchemeEntity> listWorkflowSchemes(Long tenantId) {
        return workflowSchemeMapper.toEntities(workflowSchemeRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId));
    }

    @Override
    public PageResult<WorkflowSchemeEntity> listWorkflowSchemesIncludingSystem(Long tenantId,
                                                                               WorkflowSchemeListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<WorkflowSchemeModel> result = workflowSchemeRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(workflowSchemeMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public void updateWorkflowScheme(WorkflowSchemeEntity scheme) {
        workflowSchemeRepository.save(workflowSchemeMapper.toModel(scheme));
    }

    @Override
    public void deleteWorkflowScheme(Long schemeId, Long tenantId) {
        workflowSchemeRepository.deleteByIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return workflowSchemeRepository.existsByTenantIdAndName(tenantId, name);
    }

    private Sort resolveSort(WorkflowSchemeListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of name, created_at");
        };
    }
}
