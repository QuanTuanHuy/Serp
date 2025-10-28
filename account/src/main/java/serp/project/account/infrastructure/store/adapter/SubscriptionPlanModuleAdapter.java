/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;
import serp.project.account.core.port.store.ISubscriptionPlanModulePort;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanModuleMapper;
import serp.project.account.infrastructure.store.repository.ISubscriptionPlanModuleRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanModuleAdapter implements ISubscriptionPlanModulePort {
    private final ISubscriptionPlanModuleRepository subscriptionPlanModuleRepository;
    private final SubscriptionPlanModuleMapper subscriptionPlanModuleMapper;

    @Override
    public SubscriptionPlanModuleEntity save(SubscriptionPlanModuleEntity planModule) {
        var model = subscriptionPlanModuleMapper.toModel(planModule);
        return subscriptionPlanModuleMapper.toEntity(
                subscriptionPlanModuleRepository.save(model));
    }

    @Override
    public SubscriptionPlanModuleEntity update(SubscriptionPlanModuleEntity planModule) {
        var model = subscriptionPlanModuleMapper.toModel(planModule);
        return subscriptionPlanModuleMapper.toEntity(
                subscriptionPlanModuleRepository.save(model));
    }

    @Override
    public Optional<SubscriptionPlanModuleEntity> getById(Long id) {
        return subscriptionPlanModuleRepository.findById(id)
                .map(subscriptionPlanModuleMapper::toEntity);
    }

    @Override
    public Optional<SubscriptionPlanModuleEntity> getByPlanIdAndModuleId(Long planId, Long moduleId) {
        return subscriptionPlanModuleRepository.findByPlanIdAndModuleId(planId, moduleId)
                .map(subscriptionPlanModuleMapper::toEntity);
    }

    @Override
    public List<SubscriptionPlanModuleEntity> getBySubscriptionPlanId(Long planId) {
        return subscriptionPlanModuleMapper.toEntityList(
                subscriptionPlanModuleRepository.findBySubscriptionPlanId(planId));
    }

    @Override
    public List<SubscriptionPlanModuleEntity> getByModuleId(Long moduleId) {
        return subscriptionPlanModuleMapper.toEntityList(
                subscriptionPlanModuleRepository.findByModuleId(moduleId));
    }

    @Override
    public void deleteByPlanIdAndModuleId(Long planId, Long moduleId) {
        subscriptionPlanModuleRepository.deleteBySubscriptionPlanIdAndModuleId(planId, moduleId);
    }

    @Override
    public boolean existsByPlanIdAndModuleId(Long planId, Long moduleId) {
        return subscriptionPlanModuleRepository.existsByPlanIdAndModuleId(planId, moduleId);
    }
}
