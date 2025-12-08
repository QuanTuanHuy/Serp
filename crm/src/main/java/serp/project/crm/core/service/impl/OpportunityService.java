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
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.service.ICustomerService;
import serp.project.crm.core.service.IOpportunityService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Opportunity Service - Business logic for opportunity and sales pipeline management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService implements IOpportunityService {

    private final IOpportunityPort opportunityPort;
    private final IKafkaPublisher kafkaPublisher;
    private final ICustomerService customerService;

    @Override
    @Transactional
    public OpportunityEntity createOpportunity(OpportunityEntity opportunity, Long tenantId) {
        log.info("Creating opportunity {} for tenant {}", opportunity.getName(), tenantId);

        // Validation: Customer must exist
        if (opportunity.getCustomerId() != null) {
            customerService.getCustomerById(opportunity.getCustomerId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        }

        // Set defaults using entity method
        opportunity.setTenantId(tenantId);
        opportunity.setDefaults();

        // Save
        OpportunityEntity saved = opportunityPort.save(opportunity);

        // Publish event
        publishOpportunityCreatedEvent(saved);

        log.info("Opportunity created successfully with ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public OpportunityEntity updateOpportunity(Long id, OpportunityEntity updates, Long tenantId) {
        log.info("Updating opportunity {} for tenant {}", id, tenantId);

        OpportunityEntity existing = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found"));

        // Use entity method for update
        existing.updateFrom(updates);

        // Save
        OpportunityEntity updated = opportunityPort.save(existing);

        // Publish event
        publishOpportunityUpdatedEvent(updated);

        log.info("Opportunity {} updated successfully", id);
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
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByCustomer(Long customerId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByCustomerId(customerId, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByLead(Long leadId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        
        // Port returns List, do manual pagination
        List<OpportunityEntity> allOpportunities = opportunityPort.findByLeadId(leadId, tenantId);
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allOpportunities.size());
        List<OpportunityEntity> pageContent = allOpportunities.subList(start, end);
        
        return Pair.of(pageContent, (long) allOpportunities.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesByStage(OpportunityStage stage, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.findByStage(stage, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> getOpportunitiesAssignedTo(Long userId, Long tenantId, PageRequest pageRequest) {
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
    @Transactional
    public OpportunityEntity changeStage(Long id, OpportunityStage newStage, Long tenantId) {
        log.info("Changing opportunity {} stage to {} for tenant {}", id, newStage, tenantId);

        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found"));

        // Use entity method
        OpportunityStage oldStage = opportunity.getStage();
        opportunity.advanceToStage(newStage, tenantId);

        OpportunityEntity updated = opportunityPort.save(opportunity);

        // Publish event
        publishOpportunityStageChangedEvent(updated, oldStage, newStage);

        log.info("Opportunity {} stage changed from {} to {}", id, oldStage, newStage);
        return updated;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsWon(Long id, Long tenantId) {
        log.info("Closing opportunity {} as won for tenant {}", id, tenantId);

        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found"));

        // Use entity method
        opportunity.closeAsWon();

        OpportunityEntity closed = opportunityPort.save(opportunity);

        // Update customer revenue
        if (closed.getCustomerId() != null && closed.getEstimatedValue() != null) {
            customerService.updateCustomerRevenue(
                    closed.getCustomerId(),
                    tenantId,
                    closed.getEstimatedValue(),
                    true
            );
        }

        // Publish event
        publishOpportunityWonEvent(closed);

        log.info("Opportunity {} closed as won", id);
        return closed;
    }

    @Override
    @Transactional
    public OpportunityEntity closeAsLost(Long id, String lostReason, Long tenantId) {
        log.info("Closing opportunity {} as lost for tenant {}", id, tenantId);

        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found"));

        // Use entity method
        opportunity.closeAsLost(lostReason);

        OpportunityEntity closed = opportunityPort.save(opportunity);

        // Update customer statistics (lost opportunity)
        if (closed.getCustomerId() != null) {
            customerService.updateCustomerRevenue(
                    closed.getCustomerId(),
                    tenantId,
                    BigDecimal.ZERO,
                    false
            );
        }

        // Publish event
        publishOpportunityLostEvent(closed);

        log.info("Opportunity {} closed as lost: {}", id, lostReason);
        return closed;
    }

    @Override
    @Transactional
    public void deleteOpportunity(Long id, Long tenantId) {
        log.info("Deleting opportunity {} for tenant {}", id, tenantId);

        OpportunityEntity opportunity = opportunityPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found"));

        // Validation: Cannot delete closed won opportunities
        if (opportunity.isWon()) {
            throw new IllegalStateException("Cannot delete won opportunities");
        }

        opportunityPort.deleteById(id, tenantId);

        // Publish event
        publishOpportunityDeletedEvent(opportunity);

        log.info("Opportunity {} deleted successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<OpportunityEntity>, Long> filterOpportunities(OpportunityFilterRequest filter, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return opportunityPort.filter(filter, pageRequest, tenantId);
    }

    // ========== Event Publishing ==========

    private void publishOpportunityCreatedEvent(OpportunityEntity opportunity) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity created - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityUpdatedEvent(OpportunityEntity opportunity) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity updated - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityStageChangedEvent(OpportunityEntity opportunity, OpportunityStage oldStage, OpportunityStage newStage) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity stage changed - ID: {}, {} -> {}, Topic: {}", 
                opportunity.getId(), oldStage, newStage, Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityWonEvent(OpportunityEntity opportunity) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity won - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityLostEvent(OpportunityEntity opportunity) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity lost - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }

    private void publishOpportunityDeletedEvent(OpportunityEntity opportunity) {
        // TODO: Implement event publishing
        log.debug("Event: Opportunity deleted - ID: {}, Topic: {}", opportunity.getId(), Constants.KafkaTopic.OPPORTUNITY);
    }
}
