/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.port.store.IWorkflowTransitionRulePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowTransitionRuleMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowTransitionRuleRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowTransitionRuleAdapter implements IWorkflowTransitionRulePort {

    private final IWorkflowTransitionRuleRepository workflowTransitionRuleRepository;
    private final WorkflowTransitionRuleMapper workflowTransitionRuleMapper;

    @Override
    public List<WorkflowTransitionRuleEntity> createWorkflowTransitionRules(List<WorkflowTransitionRuleEntity> rules) {
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowTransitionRuleMapper.toEntities(
                workflowTransitionRuleRepository.saveAll(workflowTransitionRuleMapper.toModels(rules))
        );
    }

    @Override
    public List<WorkflowTransitionRuleEntity> getWorkflowTransitionRulesByTransitionIdIncludingSystem(Long transitionId, Long tenantId) {
        return workflowTransitionRuleMapper.toEntities(
                workflowTransitionRuleRepository.findByTransitionIdAndTenantIdOrSystemTenant(transitionId, tenantId)
        );
    }
}
