/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.enums.SubscriptionStatus;

import java.util.List;
import java.util.Optional;

public interface IOrganizationSubscriptionPort {

    OrganizationSubscriptionEntity save(OrganizationSubscriptionEntity subscription);

    OrganizationSubscriptionEntity update(OrganizationSubscriptionEntity subscription);

    Optional<OrganizationSubscriptionEntity> getById(Long id);

    Optional<OrganizationSubscriptionEntity> getActiveByOrganizationId(Long organizationId);

    List<OrganizationSubscriptionEntity> getByOrganizationId(Long organizationId);

    List<OrganizationSubscriptionEntity> getByStatus(SubscriptionStatus status);

    /**
     * Find subscriptions expiring before the given timestamp
     */
    List<OrganizationSubscriptionEntity> getExpiringBefore(Long timestamp);

    /**
     * Find subscriptions with trial ending before the given timestamp
     */
    List<OrganizationSubscriptionEntity> getTrialEndingBefore(Long timestamp);

    boolean existsActiveSubscriptionForOrganization(Long organizationId);
}
