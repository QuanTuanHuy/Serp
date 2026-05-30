/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SurchargeRuleRepository extends JpaRepository<SurchargeRule, Long> {
    Optional<SurchargeRule> findByCode(SurchargeRuleEnum code);

    Optional<SurchargeRule> findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateIsNullOrderByEffectiveDateDesc(
            SurchargeRuleEnum code,
            LocalDate pricingDate
    );

    Optional<SurchargeRule> findFirstByCodeAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqualOrderByEffectiveDateDesc(
            SurchargeRuleEnum code,
            LocalDate effectiveDate,
            LocalDate expirationDate
    );
}
