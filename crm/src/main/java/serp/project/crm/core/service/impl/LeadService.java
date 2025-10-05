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
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.service.ILeadService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService implements ILeadService {

    private final ILeadPort leadPort;
    private final IKafkaPublisher kafkaPublisher;

    private static final int QUALIFICATION_SCORE_THRESHOLD = 70;

    @Transactional
    public LeadEntity createLead(LeadEntity lead, Long tenantId) {
        log.info("Creating lead with email {} for tenant {}", lead.getEmail(), tenantId);

        if (leadPort.existsByEmail(lead.getEmail(), tenantId)) {
            throw new IllegalArgumentException("Lead with email " + lead.getEmail() + " already exists");
        }

        lead.setTenantId(tenantId);
        lead.setDefaults();

        LeadEntity saved = leadPort.save(lead);

        publishLeadCreatedEvent(saved);

        return saved;
    }

    @Transactional
    public LeadEntity updateLead(Long id, LeadEntity updates, Long tenantId) {

        LeadEntity existing = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (leadPort.existsByEmail(updates.getEmail(), tenantId)) {
                throw new IllegalArgumentException("Lead with email " + updates.getEmail() + " already exists");
            }
        }

        existing.updateFrom(updates);

        if (updates.getLeadSource() != null || updates.getIndustry() != null ||
                updates.getCompanySize() != null || updates.getEstimatedValue() != null) {
            existing.setProbability(calculateLeadScore(existing));
        }

        LeadEntity updated = leadPort.save(existing);

        publishLeadUpdatedEvent(updated);

        return updated;
    }

    @Transactional(readOnly = true)
    public Optional<LeadEntity> getLeadById(Long id, Long tenantId) {
        return leadPort.findById(id, tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<LeadEntity> getLeadByEmail(String email, Long tenantId) {
        return leadPort.findByEmail(email, tenantId);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getAllLeads(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findAll(tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> searchLeads(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsAssignedTo(Long userId, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByAssignedTo(userId, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsBySource(LeadSource source, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByLeadSource(source, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsByStatus(LeadStatus status, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByLeadStatus(status, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getLeadsByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findByIndustry(industry, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<LeadEntity>, Long> getQualifiedLeads(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return leadPort.findQualifiedLeads(tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<LeadEntity> getLeadsByCloseDateRange(LocalDate startDate, LocalDate endDate, Long tenantId) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        Long startEpoch = startDate.toEpochDay();
        Long endEpoch = endDate.toEpochDay();
        return leadPort.findByExpectedCloseDateBetween(startEpoch, endEpoch, tenantId);
    }

    @Transactional
    public LeadEntity assignLead(Long leadId, Long userId, Long tenantId) {
        LeadEntity lead = leadPort.findById(leadId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        // TODO: Validate user exists in account service

        lead.setAssignedTo(userId);
        LeadEntity updated = leadPort.save(lead);

        publishLeadUpdatedEvent(updated);

        return updated;
    }

    @Transactional
    public LeadEntity qualifyLead(Long id, Long tenantId) {
        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        Integer leadScore = lead.getProbability() != null ? lead.getProbability() : 0;
        if (leadScore < QUALIFICATION_SCORE_THRESHOLD) {
            throw new IllegalStateException(
                    String.format("Lead score %d is below qualification threshold %d",
                            leadScore, QUALIFICATION_SCORE_THRESHOLD));
        }

        lead.qualify(tenantId, "Qualified based on score");
        LeadEntity qualified = leadPort.save(lead);

        publishLeadQualifiedEvent(qualified);

        return qualified;
    }

    @Transactional
    public LeadEntity convertLead(Long id, Long tenantId) {

        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        lead.markAsConverted(tenantId);
        LeadEntity converted = leadPort.save(lead);

        publishLeadConvertedEvent(converted);

        return converted;
    }

    @Transactional
    public void deleteLead(Long id, Long tenantId) {

        LeadEntity lead = leadPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        if (lead.getLeadStatus() == LeadStatus.CONVERTED) {
            throw new IllegalStateException("Cannot delete converted leads");
        }

        leadPort.deleteById(id, tenantId);

        publishLeadDeletedEvent(lead);

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
