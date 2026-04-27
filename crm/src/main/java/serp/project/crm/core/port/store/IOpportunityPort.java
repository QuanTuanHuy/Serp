/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IOpportunityPort {

    OpportunityEntity save(OpportunityEntity opportunityEntity);

    Optional<OpportunityEntity> findById(Long id, Long tenantId);

    Pair<List<OpportunityEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<OpportunityEntity>, Long> findByAccountId(Long accountId, Long tenantId, PageRequest pageRequest);

    List<OpportunityEntity> findByLeadId(Long leadId, Long tenantId);

    Pair<List<OpportunityEntity>, Long> findByStage(OpportunityStage stage, Long tenantId, PageRequest pageRequest);

    Pair<List<OpportunityEntity>, Long> findByAssignedTo(Long assignedTo, Long tenantId, PageRequest pageRequest);

    List<OpportunityEntity> findAllByAssignedTo(Long assignedTo, Long tenantId);

    Long countByStage(OpportunityStage stage, Long tenantId);

    BigDecimal sumEstimatedValueByStage(OpportunityStage stage, Long tenantId);

    void deleteById(Long id, Long tenantId);

    List<OpportunityEntity> findByExpectedCloseDateBetween(Long startDate, Long endDate, Long tenantId);

    List<OpportunityEntity> findWonOpportunitiesByAccountId(Long accountId, Long tenantId);

    List<OpportunityEntity> findTopByEstimatedValue(Long tenantId, int limit);

    BigDecimal calculateTotalPipelineValue(Long tenantId);

    List<OpportunityEntity> findClosingThisMonth(Long tenantId);

    Pair<List<OpportunityEntity>, Long> filter(OpportunityFilterRequest filter, PageRequest pageRequest, Long tenantId);

    List<OpportunityEntity> filterAll(OpportunityFilterRequest filter, Long tenantId);

    boolean existsByAccountIdAndName(Long accountId, String name, Long tenantId);
}
