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
import serp.project.crm.core.domain.enums.TeamMemberStatus;
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
            if (opportunityPort.existsByAccountIdAndName(
                    existing.getAccountId(), updates.getName(), tenantId)) {
                throw new AppException(ErrorMessage.OPPORTUNITY_ALREADY_EXISTS);
            }
        }
        if (updates.getAssignedTo() != null && !updates.getAssignedTo().equals(existing.getAssignedTo())) {
            teamMemberPort.findByUserId(updates.getAssignedTo(), tenantId)
                    .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus()))
                    .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));
        }

        try {
            existing.updateFrom(updates);
        } catch (IllegalArgumentException | IllegalStateException e) {
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
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByAccount(Long accountId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByAccountId(accountId, tenantId, pageRequest);
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
    public boolean existByAccountIdAndName(Long accountId, String name, Long tenantId) {
        return opportunityPort.existsByAccountIdAndName(accountId, name, tenantId);
    }

    @Override
    @Transactional
    public OpportunityEntity changeStage(Long id, OpportunityStage newStage, Long updatedBy, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        OpportunityStage oldStage = opportunity.getStage();
        try {
            opportunity.advanceToStage(newStage, updatedBy);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity updated = opportunityPort.save(opportunity);

        publishOpportunityStageChangedEvent(updated, oldStage, newStage);

        return updated;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsWon(Long id, BigDecimal actualValue, String notes, Long updatedBy, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        try {
            opportunity.closeAsWon(actualValue, notes, updatedBy);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity closed = opportunityPort.save(opportunity);

        publishOpportunityWonEvent(closed);

        return closed;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsLost(Long id, String lostReason, Long updatedBy, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));

        try {
            opportunity.closeAsLost(lostReason, updatedBy);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity closed = opportunityPort.save(opportunity);

        publishOpportunityLostEvent(closed);

        return closed;
    }

    @Override
    @Transactional
    public OpportunityEntity assignOpportunity(Long id, Long assignedTo, Long updatedBy, Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));
        teamMemberPort.findByUserId(assignedTo, tenantId)
                .filter(member -> TeamMemberStatus.ACTIVE.equals(member.getStatus()))
                .orElseThrow(() -> new AppException(ErrorMessage.TEAM_MEMBER_NOT_FOUND));

        try {
            opportunity.assignTo(assignedTo, updatedBy);
        } catch (IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity updated = opportunityPort.save(opportunity);
        publishOpportunityUpdatedEvent(updated);
        return updated;
    }

    @Override
    @Transactional
    public OpportunityEntity reopenOpportunity(Long id, OpportunityStage stage, String reopenReason, Long updatedBy,
            Long tenantId) {
        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.OPPORTUNITY_NOT_FOUND));
        OpportunityStage oldStage = opportunity.getStage();

        try {
            opportunity.reopen(stage, reopenReason, updatedBy);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new AppException(e.getMessage());
        }

        OpportunityEntity reopened = opportunityPort.save(opportunity);
        publishOpportunityStageChangedEvent(reopened, oldStage, stage);
        return reopened;
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

    @Override
    @Transactional(readOnly = true)
    public List<OpportunityEntity> filterAllOpportunities(OpportunityFilterRequest filter, Long tenantId) {
        return opportunityPort.filterAll(filter, tenantId);
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
