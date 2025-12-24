/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.infrastructure.store.mapper.OpportunityMapper;
import serp.project.crm.infrastructure.store.model.OpportunityModel;
import serp.project.crm.infrastructure.store.repository.OpportunityRepository;
import serp.project.crm.infrastructure.store.specification.OpportunitySpecification;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpportunityAdapter implements IOpportunityPort {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityMapper opportunityMapper;

    @Override
    public OpportunityEntity save(OpportunityEntity opportunityEntity) {
        var model = opportunityMapper.toModel(opportunityEntity);
        var savedModel = opportunityRepository.save(model);
        return opportunityMapper.toEntity(savedModel);
    }

    @Override
    public Optional<OpportunityEntity> findById(Long id, Long tenantId) {
        return opportunityRepository.findByIdAndTenantId(id, tenantId)
                .map(opportunityMapper::toEntity);
    }

    @Override
    public Pair<List<OpportunityEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = opportunityMapper.toPageable(pageRequest);
        var page = opportunityRepository.findByTenantId(tenantId, pageable)
                .map(opportunityMapper::toEntity);
        return opportunityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<OpportunityEntity>, Long> findByCustomerId(Long customerId, Long tenantId,
            PageRequest pageRequest) {
        var pageable = opportunityMapper.toPageable(pageRequest);
        var page = opportunityRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable)
                .map(opportunityMapper::toEntity);
        return opportunityMapper.pageToPair(page);
    }

    @Override
    public List<OpportunityEntity> findByLeadId(Long leadId, Long tenantId) {
        return opportunityRepository.findByTenantIdAndLeadId(tenantId, leadId)
                .stream()
                .map(opportunityMapper::toEntity)
                .collect(Collectors.toList());

    }

    @Override
    public Pair<List<OpportunityEntity>, Long> findByStage(OpportunityStage stage, Long tenantId,
            PageRequest pageRequest) {
        var pageable = opportunityMapper.toPageable(pageRequest);
        var page = opportunityRepository.findByTenantIdAndStage(tenantId, stage.name(), pageable)
                .map(opportunityMapper::toEntity);
        return opportunityMapper.pageToPair(page);
    }

    @Override
    public Pair<List<OpportunityEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId,
            PageRequest pageRequest) {
        var pageable = opportunityMapper.toPageable(pageRequest);
        var page = opportunityRepository.findByTenantIdAndAssignedTo(tenantId, assignedTo, pageable)
                .map(opportunityMapper::toEntity);
        return opportunityMapper.pageToPair(page);
    }

    @Override
    public Long countByStage(OpportunityStage stage, Long tenantId) {
        return opportunityRepository.countByTenantIdAndStage(tenantId, stage.name());
    }

    @Override
    public BigDecimal sumEstimatedValueByStage(OpportunityStage stage, Long tenantId) {
        BigDecimal sum = opportunityRepository.sumEstimatedValueByTenantIdAndStage(tenantId, stage.name());
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        opportunityRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(opportunityRepository::delete);
    }

    @Override
    public List<OpportunityEntity> findByExpectedCloseDateBetween(Long startDate, Long endDate, Long tenantId) {
        return List.of();
    }

    @Override
    public List<OpportunityEntity> findWonOpportunitiesByCustomerId(Long customerId, Long tenantId) {
        return List.of();
    }

    @Override
    public List<OpportunityEntity> findTopByEstimatedValue(Long tenantId, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "estimatedValue"));
        return opportunityRepository.findByTenantId(tenantId, pageable)
                .stream()
                .map(opportunityMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalPipelineValue(Long tenantId) {
        BigDecimal total = opportunityRepository.sumEstimatedValueByTenantId(tenantId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public List<OpportunityEntity> findClosingThisMonth(Long tenantId) {
        return List.of();
    }

    @Override
    public Pair<List<OpportunityEntity>, Long> filter(OpportunityFilterRequest filter, PageRequest pageRequest,
            Long tenantId) {
        var pageable = opportunityMapper.toPageable(pageRequest);
        Specification<OpportunityModel> spec = OpportunitySpecification.build(filter, tenantId);
        var page = opportunityRepository.findAll(spec, pageable)
                .map(opportunityMapper::toEntity);
        return opportunityMapper.pageToPair(page);
    }

    @Override
    public boolean existsByCustomerIdAndName(Long customerId, String name, Long tenantId) {
        return opportunityRepository.existsByTenantIdAndCustomerIdAndName(tenantId, customerId, name);
    }
}
