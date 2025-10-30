/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.infrastructure.store.model.OrganizationSubscriptionModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IOrganizationSubscriptionRepository extends IBaseRepository<OrganizationSubscriptionModel> {

    @Query("SELECT os FROM OrganizationSubscriptionModel os " +
           "WHERE os.organizationId = :organizationId " +
           "AND os.status = 'ACTIVE' " +
           "ORDER BY os.startDate DESC")
    Optional<OrganizationSubscriptionModel> findActiveByOrganizationId(@Param("organizationId") Long organizationId);

    List<OrganizationSubscriptionModel> findByOrganizationId(Long organizationId);

    List<OrganizationSubscriptionModel> findByStatus(SubscriptionStatus status);

    @Query("SELECT os FROM OrganizationSubscriptionModel os " +
           "WHERE os.status IN ('ACTIVE', 'TRIAL') " +
           "AND os.endDate IS NOT NULL " +
           "AND os.endDate < :timestamp " +
           "ORDER BY os.endDate ASC")
    List<OrganizationSubscriptionModel> findExpiringBefore(@Param("timestamp") Long timestamp);

    @Query("SELECT os FROM OrganizationSubscriptionModel os " +
           "WHERE os.status = 'TRIAL' " +
           "AND os.trialEndsAt IS NOT NULL " +
           "AND os.trialEndsAt < :timestamp " +
           "ORDER BY os.trialEndsAt ASC")
    List<OrganizationSubscriptionModel> findTrialEndingBefore(@Param("timestamp") Long timestamp);

    @Query("SELECT COUNT(os) > 0 FROM OrganizationSubscriptionModel os " +
           "WHERE os.organizationId = :organizationId " +
           "AND os.status IN ('ACTIVE', 'TRIAL')")
    boolean existsActiveSubscriptionForOrganization(@Param("organizationId") Long organizationId);

    @Query("SELECT os FROM OrganizationSubscriptionModel os " +
           "WHERE os.organizationId = :organizationId " +
           "ORDER BY os.startDate DESC")
    List<OrganizationSubscriptionModel> findByOrganizationIdOrderByStartDateDesc(@Param("organizationId") Long organizationId);

    List<OrganizationSubscriptionModel> findBySubscriptionPlanId(Long planId);
}
