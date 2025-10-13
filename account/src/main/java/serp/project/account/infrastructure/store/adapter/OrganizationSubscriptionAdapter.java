/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.port.store.IOrganizationSubscriptionPort;
import serp.project.account.infrastructure.store.mapper.OrganizationSubscriptionMapper;
import serp.project.account.infrastructure.store.repository.IOrganizationSubscriptionRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrganizationSubscriptionAdapter implements IOrganizationSubscriptionPort {
    private final IOrganizationSubscriptionRepository organizationSubscriptionRepository;
    private final OrganizationSubscriptionMapper organizationSubscriptionMapper;

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
}
