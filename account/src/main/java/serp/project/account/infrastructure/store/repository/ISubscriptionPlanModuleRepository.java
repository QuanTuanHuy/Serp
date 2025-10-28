/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.SubscriptionPlanModuleModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISubscriptionPlanModuleRepository extends IBaseRepository<SubscriptionPlanModuleModel> {

    @Query("SELECT spm FROM SubscriptionPlanModuleModel spm " +
           "WHERE spm.subscriptionPlanId = :planId AND spm.moduleId = :moduleId")
    Optional<SubscriptionPlanModuleModel> findByPlanIdAndModuleId(
            @Param("planId") Long planId, 
            @Param("moduleId") Long moduleId);

    List<SubscriptionPlanModuleModel> findBySubscriptionPlanId(Long subscriptionPlanId);

    List<SubscriptionPlanModuleModel> findByModuleId(Long moduleId);

    void deleteBySubscriptionPlanIdAndModuleId(Long subscriptionPlanId, Long moduleId);

    @Query("SELECT COUNT(spm) > 0 FROM SubscriptionPlanModuleModel spm " +
           "WHERE spm.subscriptionPlanId = :planId AND spm.moduleId = :moduleId")
    boolean existsByPlanIdAndModuleId(@Param("planId") Long planId, @Param("moduleId") Long moduleId);

    @Query("SELECT spm FROM SubscriptionPlanModuleModel spm " +
           "WHERE spm.subscriptionPlanId = :planId AND spm.isIncluded = true")
    List<SubscriptionPlanModuleModel> findIncludedModulesByPlanId(@Param("planId") Long planId);

    @Query("SELECT COUNT(spm) FROM SubscriptionPlanModuleModel spm " +
           "WHERE spm.subscriptionPlanId = :planId")
    long countBySubscriptionPlanId(@Param("planId") Long planId);
}
