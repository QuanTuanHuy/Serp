/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

import java.util.List;
import java.util.Optional;

public interface ILeadPort {

    LeadEntity save(LeadEntity leadEntity);

    Optional<LeadEntity> findById(Long id, Long tenantId);

    Optional<LeadEntity> findByEmail(String email, Long tenantId);

    Pair<List<LeadEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> findByLeadSource(LeadSource leadSource, Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> findByLeadStatus(LeadStatus leadStatus, Long tenantId, PageRequest pageRequest);

    Long countByLeadStatus(LeadStatus leadStatus, Long tenantId);

    Boolean existsByEmail(String email, Long tenantId);

    void deleteById(Long id, Long tenantId);

    Pair<List<LeadEntity>, Long> findQualifiedLeads(Long tenantId, PageRequest pageRequest);

    Pair<List<LeadEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest);

    List<LeadEntity> findByExpectedCloseDateBetween(Long startDate, Long endDate, Long tenantId);

    Pair<List<LeadEntity>, Long> filter(LeadFilterRequest filter, PageRequest pageRequest, Long tenantId);
}
