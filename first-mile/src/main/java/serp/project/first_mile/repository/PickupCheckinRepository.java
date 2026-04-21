/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PickupCheckin;

import java.util.Optional;

@Repository
public interface PickupCheckinRepository extends JpaRepository<PickupCheckin, Long> {

    Optional<PickupCheckin> findByTenantIdAndTripOrderId(Long tenantId, Long tripOrderId);
}
