/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.infrastructure.store.mapper.LeadMapper;
import serp.project.crm.infrastructure.store.model.LeadModel;
import serp.project.crm.infrastructure.store.repository.LeadRepository;
import serp.project.crm.infrastructure.store.specification.LeadSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeadAdapter implements ILeadPort {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    @Override
    public LeadEntity save(LeadEntity leadEntity) {
        var model = leadMapper.toModel(leadEntity);
        return leadMapper.toEntity(leadRepository.save(model));
    }

    @Override
    public Optional<LeadEntity> findById(Long id, Long tenantId) {
        return leadRepository.findByIdAndTenantId(id, tenantId)
                .map(leadMapper::toEntity);
    }

    @Override
    public Optional<LeadEntity> findByEmail(String email, Long tenantId) {
        return leadRepository.findByEmailAndTenantId(email, tenantId)
                .map(leadMapper::toEntity);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantId(tenantId, pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Pair<List<LeadEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.searchByKeyword(tenantId, keyword, pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId, PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantIdAndAssignedTo(tenantId, assignedTo, pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findByLeadSource(LeadSource leadSource, Long tenantId,
            PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantIdAndLeadSource(tenantId, leadSource.name(), pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findByLeadStatus(LeadStatus leadStatus, Long tenantId,
            PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantIdAndLeadStatus(tenantId, leadStatus.name(), pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Long countByLeadStatus(LeadStatus leadStatus, Long tenantId) {
        return leadRepository.countByTenantIdAndLeadStatus(tenantId, leadStatus.name());
    }

    @Override
    public Boolean existsByEmail(String email, Long tenantId) {
        return leadRepository.existsByEmailAndTenantId(email, tenantId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        leadRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(leadRepository::delete);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findQualifiedLeads(Long tenantId, PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantIdAndLeadStatus(tenantId, LeadStatus.QUALIFIED.name(), pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public Pair<List<LeadEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        var pageable = leadMapper.toPageable(pageRequest);
        var page = leadRepository.findByTenantIdAndIndustry(tenantId, industry, pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }

    @Override
    public List<LeadEntity> findByExpectedCloseDateBetween(Long startDate, Long endDate, Long tenantId) {
        return List.of();
    }

    @Override
    public Pair<List<LeadEntity>, Long> filter(LeadFilterRequest filter, PageRequest pageRequest, Long tenantId) {
        var pageable = leadMapper.toPageable(pageRequest);
        Specification<LeadModel> spec = LeadSpecification.build(filter, tenantId);
        var page = leadRepository.findAll(spec, pageable)
                .map(leadMapper::toEntity);
        return leadMapper.pageToPair(page);
    }
}
