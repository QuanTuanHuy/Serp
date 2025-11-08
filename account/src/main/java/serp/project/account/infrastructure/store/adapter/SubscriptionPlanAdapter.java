/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.GetSubscriptionPlanParams;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanMapper;
import serp.project.account.infrastructure.store.repository.ISubscriptionPlanRepository;
import serp.project.account.infrastructure.store.specification.SubscriptionPlanSpecification;
import serp.project.account.kernel.utils.PaginationUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanAdapter implements ISubscriptionPlanPort {
    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    private final PaginationUtils paginationUtils;

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

    @Override
    public Pair<List<SubscriptionPlanEntity>, Long> getAll(GetSubscriptionPlanParams params) {
        var pageable = paginationUtils.getPageable(params);
        var specification = SubscriptionPlanSpecification.getAllPlans(params);

        var result = subscriptionPlanRepository.findAll(specification, pageable);
        var entities = subscriptionPlanMapper.toEntityList(result.getContent());
        return Pair.of(entities, result.getTotalElements());
    }
}
