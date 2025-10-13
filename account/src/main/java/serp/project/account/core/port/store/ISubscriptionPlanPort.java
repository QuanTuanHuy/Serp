/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.SubscriptionPlanEntity;

import java.util.List;
import java.util.Optional;

public interface ISubscriptionPlanPort {

    SubscriptionPlanEntity save(SubscriptionPlanEntity plan);

    SubscriptionPlanEntity update(SubscriptionPlanEntity plan);

    Optional<SubscriptionPlanEntity> getById(Long id);

    Optional<SubscriptionPlanEntity> getByPlanCode(String planCode);

    List<SubscriptionPlanEntity> getAll();

    List<SubscriptionPlanEntity> getAllActive();

    Optional<SubscriptionPlanEntity> getCustomPlanByOrganizationId(Long organizationId);

    List<SubscriptionPlanEntity> getByIsCustom(Boolean isCustom);

    void delete(Long id);

    boolean existsByPlanCode(String planCode);
}
