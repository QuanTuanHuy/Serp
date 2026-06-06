/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.GetSubscriptionParams;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.port.store.IOrganizationSubscriptionPort;
import serp.project.account.infrastructure.store.mapper.OrganizationSubscriptionMapper;
import serp.project.account.infrastructure.store.model.OrganizationSubscriptionModel;
import serp.project.account.infrastructure.store.repository.IOrganizationSubscriptionRepository;
import serp.project.account.infrastructure.store.specification.SubscriptionSpecification;
import serp.project.account.kernel.utils.PaginationUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OrganizationSubscriptionAdapter implements IOrganizationSubscriptionPort {
    private final IOrganizationSubscriptionRepository organizationSubscriptionRepository;
    private final OrganizationSubscriptionMapper organizationSubscriptionMapper;

    private final PaginationUtils paginationUtils;

    @Override
    public OrganizationSubscriptionEntity save(OrganizationSubscriptionEntity subscription) {
        var model = organizationSubscriptionMapper.toModel(subscription);
        return organizationSubscriptionMapper.toEntity(
                organizationSubscriptionRepository.save(model));
    }

    @Override
    public OrganizationSubscriptionEntity update(OrganizationSubscriptionEntity subscription) {
        var model = organizationSubscriptionMapper.toModel(subscription);
        return organizationSubscriptionMapper.toEntity(
                organizationSubscriptionRepository.save(model));
    }

    @Override
    public Optional<OrganizationSubscriptionEntity> getById(Long id) {
        return organizationSubscriptionRepository.findById(id)
                .map(organizationSubscriptionMapper::toEntity);
    }

    @Override
    public Optional<OrganizationSubscriptionEntity> getActiveByOrganizationId(Long organizationId) {
        return organizationSubscriptionRepository.findActiveByOrganizationId(organizationId)
                .map(organizationSubscriptionMapper::toEntity);
    }

    @Override
    public List<OrganizationSubscriptionEntity> getByOrganizationId(Long organizationId) {
        return organizationSubscriptionMapper.toEntityList(
                organizationSubscriptionRepository.findByOrganizationId(organizationId));
    }

    @Override
    public List<OrganizationSubscriptionEntity> getByStatus(SubscriptionStatus status) {
        return organizationSubscriptionMapper.toEntityList(
                organizationSubscriptionRepository.findByStatus(status));
    }

    @Override
    public List<OrganizationSubscriptionEntity> getExpiringBefore(Long timestamp) {
        return organizationSubscriptionMapper.toEntityList(
                organizationSubscriptionRepository.findExpiringBefore(timestamp));
    }

    @Override
    public List<OrganizationSubscriptionEntity> getTrialEndingBefore(Long timestamp) {
        return organizationSubscriptionMapper.toEntityList(
                organizationSubscriptionRepository.findTrialEndingBefore(timestamp));
    }

    @Override
    public boolean existsActiveSubscriptionForOrganization(Long organizationId) {
        return organizationSubscriptionRepository
                .existsActiveSubscriptionForOrganization(organizationId);
    }

    @Override
    public Pair<List<OrganizationSubscriptionEntity>, Long> getAllSubscriptions(GetSubscriptionParams params) {
        var pageable = paginationUtils.getPageable(params);
        var specification = SubscriptionSpecification.getAllSubscriptions(params);

        var result = organizationSubscriptionRepository.findAll(specification, pageable);
        var subscriptions = organizationSubscriptionMapper.toEntityList(result.getContent());
        return Pair.of(subscriptions, result.getTotalElements());
    }

    @Override
    public List<OrganizationSubscriptionEntity> getByPlanId(Long planId) {
        return organizationSubscriptionMapper.toEntityList(
                organizationSubscriptionRepository.findBySubscriptionPlanId(planId));
    }

    @Override
    public Optional<OrganizationSubscriptionEntity> getActiveOrPendingUpgradeByOrganizationId(Long organizationId) {
        return organizationSubscriptionRepository.findActiveOrPendingUpgradeByOrganizationId(organizationId)
                .map(organizationSubscriptionMapper::toEntity);
    }

    @Override
    public Long countSubscriptions() {
        return organizationSubscriptionRepository.count();
    }

    @Override
    public Long countSubscriptionsByStatus(SubscriptionStatus status) {
        return organizationSubscriptionRepository.countByStatus(status);
    }

    @Override
    public Long countSubscriptionsEndingSoon(Long fromTimestamp, Long toTimestamp) {
        return organizationSubscriptionRepository.countActiveEndingSoon(
                toLocalDateTime(fromTimestamp),
                toLocalDateTime(toTimestamp));
    }

    @Override
    public Long countTrialsEndingSoon(Long fromTimestamp, Long toTimestamp) {
        return organizationSubscriptionRepository.countTrialsEndingSoon(
                toLocalDateTime(fromTimestamp),
                toLocalDateTime(toTimestamp));
    }

    @Override
    public Map<Long, String> getLatestSubscriptionStatusByOrganizationIds(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return Map.of();
        }
        return organizationSubscriptionRepository.findByOrganizationIdInOrderByCreatedAtDesc(organizationIds)
                .stream()
                .collect(Collectors.toMap(
                        OrganizationSubscriptionModel::getOrganizationId,
                        subscription -> subscription.getStatus().name(),
                        (existing, ignored) -> existing));
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
