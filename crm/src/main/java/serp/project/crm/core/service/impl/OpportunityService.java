/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.core.service.IOpportunityService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService implements IOpportunityService {

    private final IOpportunityPort opportunityPort;
    private final ITeamMemberPort teamMemberPort;

    @Override
    @Transactional
    public OpportunityEntity createOpportunity(OpportunityEntity opportunity, Long tenantId) {
        opportunity.setTenantId(tenantId);
        opportunity.setDefaults();

        OpportunityEntity saved = opportunityPort.save(opportunity);

        publishOpportunityCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public OpportunityEntity updateOpportunity(Long id, OpportunityEntity updates, Long tenantId) {
        OpportunityEntity existing = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));
        if (updates.getName() != null && !updates.getName().equals(existing.getName())) {
            if (opportunityPort.existsByCustomerIdAndName(
                    existing.getCustomerId(), updates.getName(), tenantId)) {
                throw new AppException(ErrorMessage.OPPORTUNITY_ALREADY_EXISTS);
            }
        }
        if (updates.getAssignedTo() != null && !updates.getAssignedTo().equals(existing.getAssignedTo())) {
            teamMemberPort.findById(updates.getAssignedTo(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
        }

        try {
            existing.updateFrom(updates);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity updated = opportunityPort.save(existing);

        publishOpportunityUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OpportunityEntity> getOpportunityById(Long id, Long tenantId) {
        return opportunityPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getAllOpportunities(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findAll(tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByCustomer(Long customerId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByCustomerId(customerId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByStage(OpportunityStage stage, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByStage(stage, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesAssignedTo(Long userId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByAssignedTo(userId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPipelineValue(Long tenantId) {
        return opportunityPort.calculateTotalPipelineValue(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getEstimatedValueByStage(OpportunityStage stage, Long tenantId) {
        return opportunityPort.sumEstimatedValueByStage(stage, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existByCustomerIdAndName(Long customerId, String name, Long tenantId) {
        return opportunityPort.existsByCustomerIdAndName(customerId, name, tenantId);
    }

    @Override
    @Transactional
    public OpportunityEntity changeStage(Long id, OpportunityStage newStage, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        OpportunityStage oldStage = opportunity.getStage();
        try {
            opportunity.advanceToStage(newStage, tenantId);
        } catch (AppException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity updated = opportunityPort.save(opportunity);

        publishOpportunityStageChangedEvent(updated, oldStage, newStage);

        return updated;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsWon(Long id, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        try {
            opportunity.closeAsWon();
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity closed = opportunityPort.save(opportunity);

        publishOpportunityWonEvent(closed);

        return closed;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsLost(Long id, String lostReason, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        try {
            opportunity.closeAsLost(lostReason);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity closed = opportunityPort.save(opportunity);

        publishOpportunityLostEvent(closed);

        return closed;
    }

    @Override
    @Transactional
    public void deleteOpportunity(Long id, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        if (opportunity.isWon()) {
            throw new AppException(ErrorMessage.CANNOT_DELETE_WON_OPPORTUNITY);
        }

        opportunityPort.deleteById(id, tenantId);

        publishOpportunityDeletedEvent(opportunity);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> filterOpportunities(OpportunityFilterRequest filter, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.filter(filter, pageRequest, tenantId);
    }

    private void publishOpportunityCreatedEvent(OpportunityEntity opportunity) {
        log.debug("Event: Opportunity created - ID: {}, Topic: {}", opportunity.getId(),
                Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityUpdatedEvent(OpportunityEntity opportunity) {
        log.debug("Event: Opportunity updated - ID: {}, Topic: {}", opportunity.getId(),
                Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityStageChangedEvent(OpportunityEntity opportunity, OpportunityStage oldStage,
            OpportunityStage newStage) {
        log.debug("Event: Opportunity stage changed - ID: {}, {} -> {}, Topic: {}",
                opportunity.getId(), oldStage, newStage, Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityWonEvent(OpportunityEntity opportunity) {
        log.debug("Event: Opportunity won - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityLostEvent(OpportunityEntity opportunity) {
        log.debug("Event: Opportunity lost - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityDeletedEvent(OpportunityEntity opportunity) {
        log.debug("Event: Opportunity deleted - ID: {}, Topic: {}", opportunity.getId(),
                Constants.KafkaTopic.OPPORTUNITY);
    }
}
