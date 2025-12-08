/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ILeadService {

    LeadEntity createLead(LeadEntity lead, Long tenantId);

    LeadEntity updateLead(Long id, LeadEntity updates, Long tenantId);

    Optional<LeadEntity> getLeadById(Long id, Long tenantId);

    Optional<LeadEntity> getLeadByEmail(String email, Long tenantId);

    Pair<List<LeadEntity>, Long> getAllLeads(Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> searchLeads(String keyword, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> getLeadsAssignedTo(Long userId, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> getLeadsBySource(LeadSource source, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> getLeadsByStatus(LeadStatus status, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> getLeadsByIndustry(String industry, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> getQualifiedLeads(Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> filterLeads(LeadFilterRequest filter, Long tenantId, PageRequest pageRequest);

    List<LeadEntity> getLeadsByCloseDateRange(LocalDate startDate, LocalDate endDate, Long tenantId);

    LeadEntity assignLead(Long leadId, Long userId, Long tenantId);

    LeadEntity qualifyLead(Long id, Long tenantId);

    LeadEntity convertLead(Long id, Long tenantId);

    LeadEntity convertLead(Long id, Long customerId, Long opportunityId, Long tenantId);

    void deleteLead(Long id, Long tenantId);
}
