/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IOpportunityService {

    OpportunityEntity createOpportunity(OpportunityEntity opportunity, Long tenantId);

    OpportunityEntity updateOpportunity(Long id, OpportunityEntity updates, Long tenantId);

    Optional<OpportunityEntity> getOpportunityById(Long id, Long tenantId);

    boolean existByAccountIdAndName(Long accountId, String name, Long tenantId);

    Pair<List<OpportunityEntity>, Long> getAllOpportunities(Long tenantId, PageRequest pageRequest);

    Pair<List<OpportunityEntity>, Long> getOpportunitiesByAccount(Long accountId, Long tenantId,
            PageRequest pageRequest);

    Pair<List<OpportunityEntity>, Long> getOpportunitiesByStage(OpportunityStage stage, Long tenantId,
            PageRequest pageRequest);

    Pair<List<OpportunityEntity>, Long> getOpportunitiesAssignedTo(Long userId, Long tenantId,
            PageRequest pageRequest);

    BigDecimal getTotalPipelineValue(Long tenantId);

    BigDecimal getEstimatedValueByStage(OpportunityStage stage, Long tenantId);

    OpportunityEntity changeStage(Long id, OpportunityStage newStage, Long updatedBy, Long tenantId);

    OpportunityEntity closeAsWon(Long id, BigDecimal actualValue, String notes, Long updatedBy, Long tenantId);

    OpportunityEntity closeAsLost(Long id, String lostReason, Long updatedBy, Long tenantId);

    OpportunityEntity assignOpportunity(Long id, Long assignedTo, Long updatedBy, Long tenantId);

    OpportunityEntity reopenOpportunity(Long id, OpportunityStage stage, String reopenReason, Long updatedBy, Long tenantId);

    void deleteOpportunity(Long id, Long tenantId);

    Pair<List<OpportunityEntity>, Long> filterOpportunities(OpportunityFilterRequest filter, Long tenantId,
            PageRequest pageRequest);

    List<OpportunityEntity> filterAllOpportunities(OpportunityFilterRequest filter, Long tenantId);

    List<OpportunityEntity> getOpportunitiesByIds(List<Long> ids, Long tenantId);
}
