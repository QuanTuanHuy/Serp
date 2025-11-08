/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.dto.request.GetSubscriptionPlanParams;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.util.Pair;

public interface ISubscriptionPlanPort {

    SubscriptionPlanEntity save(SubscriptionPlanEntity plan);

    SubscriptionPlanEntity update(SubscriptionPlanEntity plan);

    Optional<SubscriptionPlanEntity> getById(Long id);

    Optional<SubscriptionPlanEntity> getByPlanCode(String planCode);

    List<SubscriptionPlanEntity> getAll();

    Pair<List<SubscriptionPlanEntity>, Long> getAll(GetSubscriptionPlanParams params);

    List<SubscriptionPlanEntity> getAllActive();

    Optional<SubscriptionPlanEntity> getCustomPlanByOrganizationId(Long organizationId);

    List<SubscriptionPlanEntity> getByIsCustom(Boolean isCustom);

    void delete(Long id);

    boolean existsByPlanCode(String planCode);
}
