/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.VasRule;
import serp.project.tms_billing_service.enums.VasRuleCode;

import java.util.Optional;

@Repository
public interface VasRuleRepository extends JpaRepository<VasRule, Long> {
    Optional<VasRule> findByCode(VasRuleCode code);
}
