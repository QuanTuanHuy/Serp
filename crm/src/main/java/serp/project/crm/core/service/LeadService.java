/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.ILeadPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lead Service - Business logic for lead management and qualification
 * Responsibilities:
 * - Lead CRUD operations with validation
 * - Lead scoring algorithm
 * - Lead qualification and conversion
 * - Assignment management
 * - Event publishing for lead lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService implements ILeadService {

    private final ILeadPort leadPort;
    private final IKafkaPublisher kafkaPublisher;

    private static final int QUALIFICATION_SCORE_THRESHOLD = 70;

    /**
     * Create new lead
     * Business rules:
     * - Email must be unique within tenant
     * - Default status is NEW
     * - Calculate initial lead score
     */
    @Transactional
    public LeadEntity createLead(LeadEntity lead, Long tenantId) {
        log.info("Creating lead with email {} for tenant {}", lead.getEmail(), tenantId);

        // Validation: Email uniqueness
        if (leadPort.existsByEmail(lead.getEmail(), tenantId)) {
            throw new IllegalArgumentException("Lead with email " + lead.getEmail() + " already exists");
        }

        // Set defaults
        lead.setTenantId(tenantId);
        lead.setDefaults();

        // Save
        LeadEntity saved = leadPort.save(lead);

        // Publish event
        publishLeadCreatedEvent(saved);

        log.info("Lead created successfully with ID {}", saved.getId());
        return saved;
    }

    /**
     * Update existing lead
     * Business rules:
     * - Lead must exist
     * - Recalculate score if relevant fields changed
     * - Cannot change email to existing one
     */
    @Transactional
    public LeadEntity updateLead(Long id, LeadEntity updates, Long tenantId) {
        log.info("Updating lead {} for tenant {}", id, tenantId);

        LeadEntity existing = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // Validation: Email uniqueness if changed
        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (leadPort.existsByEmail(updates.getEmail(), tenantId)) {
                throw new IllegalArgumentException("Lead with email " + updates.getEmail() + " already exists");
            }
        }

        // Use entity method for update
        existing.updateFrom(updates);

        // Recalculate probability if source/industry/value changed
        if (updates.getLeadSource() != null || updates.getIndustry() != null || 
            updates.getCompanySize() != null || updates.getEstimatedValue() != null) {
            existing.setProbability(calculateLeadScore(existing));
        }

        // Save
        LeadEntity updated = leadPort.save(existing);

        // Publish event
        publishLeadUpdatedEvent(updated);

        log.info("Lead {} updated successfully", id);
        return updated;
    }

    /**
     * Get lead by ID
     */
    @Transactional(readOnly = true)
    public Optional<LeadEntity> getLeadById(Long id, Long tenantId) {
        return leadPort.findById(id, tenantId);
    }

    /**
     * Get lead by email
     */
    @Transactional(readOnly = true)
    public Optional<LeadEntity> getLeadByEmail(String email, Long tenantId) {
        return leadPort.findByEmail(email, tenantId);
    }

    /**
     * Get all leads with pagination
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getAllLeads(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findAll(tenantId, pageRequest);
    }

    /**
     * Search leads by keyword
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> searchLeads(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    /**
     * Get leads assigned to user
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsAssignedTo(Long userId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByAssignedTo(userId, tenantId, pageRequest);
    }

    /**
     * Get leads by source
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsBySource(LeadSource source, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByLeadSource(source, tenantId, pageRequest);
    }

    /**
     * Get leads by status
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsByStatus(LeadStatus status, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByLeadStatus(status, tenantId, pageRequest);
    }

    /**
     * Get leads by industry
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByIndustry(industry, tenantId, pageRequest);
    }

    /**
     * Get qualified leads (score >= threshold)
     */
    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getQualifiedLeads(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findQualifiedLeads(tenantId, pageRequest);
    }

    /**
     * Get leads by expected close date range
     */
    @Transactional(readOnly = true)
    public List<LeadEntity> getLeadsByCloseDateRange(LocalDate startDate, LocalDate endDate, Long tenantId) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        // Convert LocalDate to Long (epoch days)
        Long startEpoch = startDate.toEpochDay();
        Long endEpoch = endDate.toEpochDay();
        return leadPort.findByExpectedCloseDateBetween(startEpoch, endEpoch, tenantId);
    }

    /**
     * Assign lead to user
     */
    @Transactional
    public LeadEntity assignLead(Long leadId, Long userId, Long tenantId) {
        log.info("Assigning lead {} to user {} for tenant {}", leadId, userId, tenantId);

        LeadEntity lead = leadPort.findById(leadId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // TODO: Validate user exists in account service

        lead.setAssignedTo(userId);
        LeadEntity updated = leadPort.save(lead);

        publishLeadUpdatedEvent(updated);

        log.info("Lead {} assigned to user {} successfully", leadId, userId);
        return updated;
    }

    /**
     * Qualify lead
     * Business rules:
     * - Lead score must be >= threshold
     * - Status changes to QUALIFIED
     */
    @Transactional
    public LeadEntity qualifyLead(Long id, Long tenantId) {
        log.info("Qualifying lead {} for tenant {}", id, tenantId);

        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // Validation: Lead score threshold
        Integer leadScore = lead.getProbability() != null ? lead.getProbability() : 0;
        if (leadScore < QUALIFICATION_SCORE_THRESHOLD) {
            throw new IllegalStateException(
                    String.format("Lead score %d is below qualification threshold %d",
                            leadScore, QUALIFICATION_SCORE_THRESHOLD));
        }

        // Use entity method
        lead.qualify(tenantId, "Qualified based on score");
        LeadEntity qualified = leadPort.save(lead);

        // Publish event
        publishLeadQualifiedEvent(qualified);

        log.info("Lead {} qualified successfully", id);
        return qualified;
    }

    /**
     * Mark lead as converted
     * Business rules:
     * - Lead must be qualified
     * - Status changes to CONVERTED
     */
    @Transactional
    public LeadEntity convertLead(Long id, Long tenantId) {
        log.info("Converting lead {} for tenant {}", id, tenantId);

        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // Use entity method (will validate internally)
        lead.markAsConverted(tenantId);
        LeadEntity converted = leadPort.save(lead);

        // Publish event
        publishLeadConvertedEvent(converted);

        log.info("Lead {} converted successfully", id);
        return converted;
    }

    /**
     * Delete lead
     */
    @Transactional
    public void deleteLead(Long id, Long tenantId) {
        log.info("Deleting lead {} for tenant {}", id, tenantId);

        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // Validation: Cannot delete converted leads
        if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
            throw new IllegalStateException("Cannot delete converted leads");
        }

        leadPort.deleteById(id, tenantId);

        // Publish event
        publishLeadDeletedEvent(lead);

        log.info("Lead {} deleted successfully", id);
    }

    // ========== Business Logic ==========

    /**
     * Calculate lead score based on multiple factors
     * Scoring algorithm:
     * - Lead source: 30 points (referral highest)
     * - Industry: 20 points (technology/finance highest)
     * - Estimated value: 30 points (higher = better)
     * - Company size: 20 points (larger = better)
     * Total: 100 points
     */
    private Integer calculateLeadScore(LeadEntity lead) {
        int score = 0;

        // Lead source scoring (0-30 points)
        if (lead.getLeadSource() != null) {
            score += switch (lead.getLeadSource()) {
                case REFERRAL -> 30;
                case SOCIAL_MEDIA -> 20;
                case EMAIL_CAMPAIGN -> 15;
                case WEBSITE -> 10;
                case COLD_CALL -> 5;
            };
        }

        // Industry scoring (0-20 points)
        if (lead.getIndustry() != null) {
            String industry = lead.getIndustry().toLowerCase();
            if (industry.contains("technology") || industry.contains("software")) {
                score += 20;
            } else if (industry.contains("finance") || industry.contains("healthcare")) {
                score += 15;
            } else if (industry.contains("retail") || industry.contains("manufacturing")) {
                score += 10;
            } else {
                score += 5;
            }
        }

        // Estimated value scoring (0-30 points)
        if (lead.getEstimatedValue() != null) {
            double value = lead.getEstimatedValue().doubleValue();
            if (value >= 1_000_000) {
                score += 30;
            } else if (value >= 500_000) {
                score += 25;
            } else if (value >= 100_000) {
                score += 20;
            } else if (value >= 50_000) {
                score += 15;
            } else if (value >= 10_000) {
                score += 10;
            } else {
                score += 5;
            }
        }

        // Company size scoring (0-20 points)
        if (lead.getCompanySize() != null) {
            String size = lead.getCompanySize().toLowerCase();
            if (size.contains("enterprise") || size.contains("1000+")) {
                score += 20;
            } else if (size.contains("large") || size.contains("500")) {
                score += 15;
            } else if (size.contains("medium") || size.contains("100")) {
                score += 10;
            } else {
                score += 5;
            }
        }

        return score;
    }

    // ========== Event Publishing ==========

    private void publishLeadCreatedEvent(LeadEntity lead) {
        // TODO: Implement event publishing
        log.debug("Event: Lead created - ID: {}, Topic: {}", lead.getId(), Constants.KafkaTopic.LEAD);
    }

    private void publishLeadUpdatedEvent(LeadEntity lead) {
        // TODO: Implement event publishing
        log.debug("Event: Lead updated - ID: {}, Topic: {}", lead.getId(), Constants.KafkaTopic.LEAD);
    }

    private void publishLeadQualifiedEvent(LeadEntity lead) {
        // TODO: Implement event publishing
        log.debug("Event: Lead qualified - ID: {}, Topic: {}", lead.getId(), Constants.KafkaTopic.LEAD);
    }

    private void publishLeadConvertedEvent(LeadEntity lead) {
        // TODO: Implement event publishing
        log.debug("Event: Lead converted - ID: {}, Topic: {}", lead.getId(), Constants.KafkaTopic.LEAD);
    }

    private void publishLeadDeletedEvent(LeadEntity lead) {
        // TODO: Implement event publishing
        log.debug("Event: Lead deleted - ID: {}, Topic: {}", lead.getId(), Constants.KafkaTopic.LEAD);
    }
}
