/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.SubscriptionPlanModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISubscriptionPlanRepository extends IBaseRepository<SubscriptionPlanModel> {

    Optional<SubscriptionPlanModel> findByPlanCode(String planCode);

    @Query("SELECT sp FROM SubscriptionPlanModel sp WHERE sp.isActive = true ORDER BY sp.displayOrder ASC")
    List<SubscriptionPlanModel> findAllActive();

    @Query("SELECT sp FROM SubscriptionPlanModel sp " +
           "WHERE sp.isCustom = true AND sp.organizationId = :organizationId")
    Optional<SubscriptionPlanModel> findCustomPlanByOrganizationId(@Param("organizationId") Long organizationId);

    List<SubscriptionPlanModel> findByIsCustom(Boolean isCustom);

    boolean existsByPlanCode(String planCode);

    @Query("SELECT COUNT(sp) > 0 FROM SubscriptionPlanModel sp " +
           "WHERE sp.planCode = :planCode AND sp.id != :excludeId")
    boolean existsByPlanCodeAndIdNot(@Param("planCode") String planCode, @Param("excludeId") Long excludeId);
}
