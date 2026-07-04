/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.ChargeableWeightConfig;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargeableWeightConfigRepository extends JpaRepository<ChargeableWeightConfig, Long> {
    Optional<ChargeableWeightConfig> findByServiceCode(String serviceCode);

    List<ChargeableWeightConfig> findAllByOrderByServiceCodeAsc();
}
