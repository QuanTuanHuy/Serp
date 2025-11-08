/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.SubscriptionPlanModuleEntity;

import java.util.List;
import java.util.Optional;

public interface ISubscriptionPlanModulePort {

    SubscriptionPlanModuleEntity save(SubscriptionPlanModuleEntity planModule);

    void saveAll(List<SubscriptionPlanModuleEntity> planModules);

    SubscriptionPlanModuleEntity update(SubscriptionPlanModuleEntity planModule);

    Optional<SubscriptionPlanModuleEntity> getById(Long id);

    Optional<SubscriptionPlanModuleEntity> getByPlanIdAndModuleId(Long planId, Long moduleId);

    List<SubscriptionPlanModuleEntity> getBySubscriptionPlanId(Long planId);

    List<SubscriptionPlanModuleEntity> getByModuleId(Long moduleId);

    void deleteByPlanIdAndModuleId(Long planId, Long moduleId);

    boolean existsByPlanIdAndModuleId(Long planId, Long moduleId);
}
