/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanMapper;
import serp.project.account.infrastructure.store.repository.ISubscriptionPlanRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanAdapter implements ISubscriptionPlanPort {
    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public SubscriptionPlanEntity save(SubscriptionPlanEntity plan) {
        var model = subscriptionPlanMapper.toModel(plan);
        return subscriptionPlanMapper.toEntity(subscriptionPlanRepository.save(model));
    }

    @Override
    public SubscriptionPlanEntity update(SubscriptionPlanEntity plan) {
        var model = subscriptionPlanMapper.toModel(plan);
        return subscriptionPlanMapper.toEntity(subscriptionPlanRepository.save(model));
    }

    @Override
    public Optional<SubscriptionPlanEntity> getById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .map(subscriptionPlanMapper::toEntity);
    }

    @Override
    public Optional<SubscriptionPlanEntity> getByPlanCode(String planCode) {
        return subscriptionPlanRepository.findByPlanCode(planCode)
                .map(subscriptionPlanMapper::toEntity);
    }

    @Override
    public List<SubscriptionPlanEntity> getAll() {
        return subscriptionPlanMapper.toEntityList(subscriptionPlanRepository.findAll());
    }

    @Override
    public List<SubscriptionPlanEntity> getAllActive() {
        return subscriptionPlanMapper.toEntityList(subscriptionPlanRepository.findAllActive());
    }

    @Override
    public Optional<SubscriptionPlanEntity> getCustomPlanByOrganizationId(Long organizationId) {
        return subscriptionPlanRepository.findCustomPlanByOrganizationId(organizationId)
                .map(subscriptionPlanMapper::toEntity);
    }

    @Override
    public List<SubscriptionPlanEntity> getByIsCustom(Boolean isCustom) {
        return subscriptionPlanMapper.toEntityList(
                subscriptionPlanRepository.findByIsCustom(isCustom));
    }

    @Override
    public void delete(Long id) {
        subscriptionPlanRepository.deleteById(id);
    }

    @Override
    public boolean existsByPlanCode(String planCode) {
        return subscriptionPlanRepository.existsByPlanCode(planCode);
    }
}
