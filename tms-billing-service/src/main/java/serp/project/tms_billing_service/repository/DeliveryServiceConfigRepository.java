/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.DeliveryServiceConfig;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryServiceConfigRepository extends JpaRepository<DeliveryServiceConfig, Long> {
    Optional<DeliveryServiceConfig> findByServiceCode(String serviceCode);

    List<DeliveryServiceConfig> findAllByActiveTrueOrderBySortOrderAscServiceCodeAsc();

    List<DeliveryServiceConfig> findAllByOrderBySortOrderAscServiceCodeAsc();
}
