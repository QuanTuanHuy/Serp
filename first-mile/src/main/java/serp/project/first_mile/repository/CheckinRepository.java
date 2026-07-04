/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Checkin;
import serp.project.first_mile.enums.CheckinType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    Optional<Checkin> findByTenantIdAndCheckinTypeAndTripOrderId(
            Long tenantId, CheckinType checkinType, Long tripOrderId);

    Optional<Checkin> findByTenantIdAndCheckinTypeAndOrderId(
            Long tenantId, CheckinType checkinType, Long orderId);

    long countByTenantIdAndCheckinTypeAndTripId(Long tenantId, CheckinType checkinType, Long tripId);

    Optional<Checkin> findByTenantIdAndCheckinTypeAndDeliveryManifestOrderId(
            Long tenantId, CheckinType checkinType, Long deliveryManifestOrderId);

    List<Checkin> findByTenantIdAndCheckinTypeAndTripOrderIdIn(
            Long tenantId, CheckinType checkinType, Collection<Long> tripOrderIds);
}
